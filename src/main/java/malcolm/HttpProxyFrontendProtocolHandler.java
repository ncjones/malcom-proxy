/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package malcolm;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Manipulates the current pipeline dynamically to switch protocols or enable
 * SSL or GZIP.
 */
public class HttpProxyFrontendProtocolHandler extends ByteToMessageDecoder {

  private final SslContext serverSslCtx;
  private final SslContext clientSslCtx;
  private final boolean sslDetected;
  private final boolean gzipDetected;

  public HttpProxyFrontendProtocolHandler(final SslContext serverSslCtx, final SslContext clientSslCtx) {
    this(serverSslCtx, clientSslCtx, false, false);
  }

  private HttpProxyFrontendProtocolHandler(final SslContext sslCtx, final SslContext clientSslCtx,
      final boolean sslDetected, final boolean gzipDetected) {
    this.serverSslCtx = sslCtx;
    this.clientSslCtx = clientSslCtx;
    this.sslDetected = sslDetected;
    this.gzipDetected = gzipDetected;
  }

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
    // Will use the first five bytes to detect a protocol.
    if (in.readableBytes() < 5) {
      return;
    }
    if (isSsl(in)) {
      enableSsl(ctx);
    } else {
      final int magic1 = in.getUnsignedByte(in.readerIndex());
      final int magic2 = in.getUnsignedByte(in.readerIndex() + 1);
      if (isGzip(magic1, magic2)) {
        enableGzip(ctx);
      } else if (isHttp(magic1, magic2)) {
        switchToHttp(ctx);
      } else {
        // Unknown protocol; discard everything and close the connection.
        in.clear();
        ctx.close();
      }
    }
  }

  private boolean isSsl(final ByteBuf buf) {
    if (!sslDetected) {
      return SslHandler.isEncrypted(buf);
    }
    return false;
  }

  private boolean isGzip(final int magic1, final int magic2) {
    if (!gzipDetected) {
      return magic1 == 31 && magic2 == 139;
    }
    return false;
  }

  private static boolean isHttp(final int magic1, final int magic2) {
    return Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE", "CONNECT").stream()
        .anyMatch(method -> method.charAt(0) == magic1 && method.charAt(1) == magic2);
  }

  private void enableSsl(final ChannelHandlerContext ctx) {
    ctx.pipeline()
      .addLast("ssl", serverSslCtx.newHandler(ctx.alloc()))
      .addLast("unificationA", new HttpProxyFrontendProtocolHandler(serverSslCtx, clientSslCtx, true, gzipDetected))
      .remove(this);
  }

  private void enableGzip(final ChannelHandlerContext ctx) {
    ctx.pipeline()
      .addLast("gzipdeflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP))
      .addLast("gzipinflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP))
      .addLast("unificationB", new HttpProxyFrontendProtocolHandler(serverSslCtx, clientSslCtx, sslDetected, true))
      .remove(this);
  }

  private void switchToHttp(final ChannelHandlerContext ctx) {
    ctx.pipeline()
      .addLast("decoder", new HttpRequestDecoder())
      .addLast("encoder", new HttpResponseEncoder())
      .addLast("deflater", new HttpContentCompressor())
      .addLast("handler", new HttpProxyFrontendHandler(sslDetected ? Optional.of(clientSslCtx) : Optional.empty()))
      .remove(this);
  }

}

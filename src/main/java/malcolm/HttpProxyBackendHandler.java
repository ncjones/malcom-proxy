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

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxyBackendHandler extends ChannelInboundHandlerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(HttpProxyBackendHandler.class);

  private final Channel frontendChannel;

  public HttpProxyBackendHandler(final Channel frontendChannel) {
    this.frontendChannel = frontendChannel;
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) {
    // TODO what does this to?
    ctx.read();
    ctx.write(Unpooled.EMPTY_BUFFER);
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
    if (msg instanceof HttpResponse) {
      channelReadHttpResponse(ctx, (HttpResponse) msg);
    }
    if (msg instanceof HttpContent) {
      channelReadHttpContent(ctx, (HttpContent) msg);
    }
  }

  private void writeHttpMessage(final ChannelHandlerContext ctx, final HttpObject obj) {
    frontendChannel.writeAndFlush(obj).addListener(f -> {
      if (f.isSuccess()) {
        ctx.channel().read();
      } else {
        logger.debug("write response failed");
        ctx.channel().close();
      }
    });
  }

  private void channelReadHttpResponse(final ChannelHandlerContext ctx, final HttpResponse response) {
    writeHttpMessage(ctx, response);
  }

  private void channelReadHttpContent(final ChannelHandlerContext ctx, final HttpContent content) {
    writeHttpMessage(ctx, content);
  }

  @Override
  public void channelInactive(final ChannelHandlerContext ctx) {
    closeOnFlush(frontendChannel);
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    logger.info("Error processing backend request", cause);
    closeOnFlush(ctx.channel());
  }

  private static void closeOnFlush(final Channel ch) {
    if (ch.isActive()) {
      ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
  }
}

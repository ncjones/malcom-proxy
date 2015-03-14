/*
 * Copyright 2013 The Netty Project
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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpProxyFrontendHandler extends ChannelInboundHandlerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(HttpProxyFrontendHandler.class);

  @Override
  public void channelReadComplete(final ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
    if (msg instanceof HttpRequest) {
      channelReadHttpRequest(ctx, (HttpRequest) msg);
    }
  }

  private void channelReadHttpRequest(final ChannelHandlerContext ctx, final HttpRequest req) {
    System.out.println(req.getMethod() + " " + req.getUri());
    this.proxyRequest(ctx, req, f -> {
      if (f.isSuccess()) {
        logger.debug("write complete");
        // TODO f.channel().read() ?
      } else {
        logger.debug("write failed");
        // TODO bad gateway?
      }
    });
  }

  private Channel proxyRequest(final ChannelHandlerContext ctx, final HttpRequest req,
      final ChannelFutureListener onWriteComplete) {
    final Endpoint endpoint = HttpUtil.getEndpoint(req)
        .orElseThrow(() -> new IllegalArgumentException("Missing host header"));
    return backendChannelBootstap(ctx.channel())
        .connect(endpoint.getHost(), endpoint.getPort())
        .addListener((final ChannelFuture f) -> {
          if (f.isSuccess()) {
            f.channel().writeAndFlush(req).addListener(onWriteComplete);
          } else {
            logger.debug("connect failed");
            // TODO bad gateway?
          }
        }).channel();
  }

  private Bootstrap backendChannelBootstap(final Channel frontendChannel) {
    return new Bootstrap()
      .group(frontendChannel.eventLoop())
      .channel(NioSocketChannel.class)
      .handler(new HttpProxyBackendInitializer(frontendChannel))
      .option(ChannelOption.AUTO_READ, false);
  }

  @Override
  public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
    logger.info("Error processing frontend request", cause);
    ctx.close();
  }
}

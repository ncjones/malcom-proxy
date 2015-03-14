/*
 * Copyright Nathan Jones 2015
 *
 * This file is part of Malcom Proxy.
 *
 * Malcom Proxy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Malcom Proxy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Malcom Proxy.  If not, see <http://www.gnu.org/licenses/>.
 */
package malcolm;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class HttpProxyServer {

  private final int port;

  public HttpProxyServer(final int port) {
    this.port = port;
  }

  public void start() throws Exception {
    final SelfSignedCertificate ssc = new SelfSignedCertificate();
    final SslContext serverSslContext = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
    final SslContext clientSslContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
    final EventLoopGroup bossGroup = new NioEventLoopGroup();
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    final ChannelFuture channelFuture = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .handler(new LoggingHandler(LogLevel.DEBUG))
        .childHandler(new HttpProxyFrontendInitializer(serverSslContext, clientSslContext))
        .bind(port);
    try {
      channelFuture.sync().channel().closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

}

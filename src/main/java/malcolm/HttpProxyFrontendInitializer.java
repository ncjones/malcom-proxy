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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

public class HttpProxyFrontendInitializer extends
    ChannelInitializer<SocketChannel> {

  private final SslContext serverSslCtx;
  private final SslContext clientSslCtx;

  public HttpProxyFrontendInitializer(final SslContext serverSslCtx, final SslContext clientSslCtx) {
    this.serverSslCtx = serverSslCtx;
    this.clientSslCtx = clientSslCtx;
  }

  @Override
  public void initChannel(final SocketChannel ch) {
    ch.pipeline()
        .addLast(new LoggingHandler(LogLevel.DEBUG))
        .addLast(new HttpProxyFrontendProtocolHandler(serverSslCtx, clientSslCtx));
  }

}

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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpProxyBackendInitializer extends ChannelInitializer<SocketChannel> {

  private final Channel frontendChannel;

  public HttpProxyBackendInitializer(final Channel frontendChannel) {
    this.frontendChannel = frontendChannel;
  }

  @Override
  public void initChannel(final SocketChannel ch) {
    ch.pipeline()
        .addLast(new LoggingHandler(LogLevel.DEBUG))
        .addLast(new HttpClientCodec())
        .addLast(new HttpContentDecompressor())
        .addLast(new HttpProxyBackendHandler(frontendChannel));
  }

}

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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

import java.util.Optional;

public class HttpUtil {

  public static Optional<Endpoint> getEndpoint(final HttpMessage msg) {
    String hostHeader = HttpHeaders.getHost(msg);
    if (hostHeader == null) {
      return Optional.empty();
    }
    final String[] hostAndPort = hostHeader.split(":");
    return Optional.of(new Endpoint(getHost(hostAndPort), getPort(hostAndPort)));
  }

  private static String getHost(final String[] hostAndPort) {
    return hostAndPort[0];
  }

  private static int getPort(final String[] hostAndPort) {
    if (hostAndPort.length != 2) {
      return 80;
    }
    return Integer.parseInt(hostAndPort[1]);
  }

}

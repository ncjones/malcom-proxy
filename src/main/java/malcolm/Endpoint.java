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

public class Endpoint {

  private final String host;

  private final int port;

  public Endpoint(final String host, final int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

}

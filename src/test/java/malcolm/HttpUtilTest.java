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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpUtilTest {

  @Mock
  private Attribute<Boolean> mockAttribute;

  @Test
  public void getEndpointShouldReturnRequestHost() {
    final HttpMessage msg = httpRequest("example.com");
    assertThat(HttpUtil.getEndpoint(msg).get().getHost(), is("example.com"));
  }

  @Test
  public void getEndpointShouldReturnRequestHostWithoutPort() {
    final HttpMessage msg = httpRequest("example.com:8080");
    assertThat(HttpUtil.getEndpoint(msg).get().getHost(), is("example.com"));
  }

  @Test
  public void getEndpointShouldReturnPortFromHost() {
    final HttpMessage msg = httpRequest("example.com:8080");
    assertThat(HttpUtil.getEndpoint(msg).get().getPort(), is(8080));
  }

  @Test
  public void getEndpointShouldReturnPort80WhenHostHasNoPort() {
    final HttpMessage msg = httpRequest("example.com");
    when(mockAttribute.get()).thenReturn(false);
    assertThat(HttpUtil.getEndpoint(msg).get().getPort(), is(80));
  }

  @Test
  public void getEndpointShouldBeAbsentWhenHostHeaderMissing() {
    final HttpMessage msg = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
    when(mockAttribute.get()).thenReturn(false);
    assertThat(HttpUtil.getEndpoint(msg).isPresent(), is(false));
  }

  private HttpRequest httpRequest(final String hostname) {
    final HttpRequest msg = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
    msg.headers().add("Host", hostname);
    return msg;
  }

}

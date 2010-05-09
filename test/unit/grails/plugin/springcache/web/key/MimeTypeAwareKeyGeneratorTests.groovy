/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web.key

import grails.plugin.springcache.web.FilterContext
import javax.servlet.http.HttpServletRequest
import org.gmock.WithGMock
import org.junit.Test
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

@WithGMock
class MimeTypeAwareKeyGeneratorTests extends GroovyTestCase {

	KeyGenerator generator = new MimeTypeAwareKeyGenerator()

	@Test
	void keyVariesByMimeType() {
		def request = mock(HttpServletRequest) {
			format.returns("html").times(2)
			format.returns("xml")
		}
		play {
			def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", request: request))
			def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", request: request))
			def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", request: request))

			assertThat key1, equalTo(key2)
			assertThat key1, not(equalTo(key3))
		}
	}

	@Test
	void formatOfAllIsIgnored() {
		def request = mock(HttpServletRequest) {
			format.returns("all")
			format.returns(null)
		}
		play {
			def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", request: request))
			def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", request: request))

			assertThat key1, equalTo(key2)
		}
	}

}

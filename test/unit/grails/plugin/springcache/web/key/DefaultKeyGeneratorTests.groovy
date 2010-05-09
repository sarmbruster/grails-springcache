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
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

class DefaultKeyGeneratorTests extends GroovyTestCase {

	KeyGenerator generator = new DefaultKeyGenerator()

	@Test
	void keyVariesOnControllerName() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key3 = generator.generateKey(new FilterContext(controllerName: "baz", actionName: "bar"))

		assertThat key1, equalTo(key2)
		assertThat key1, not(equalTo(key3))
	}

	@Test
	void keyVariesOnActionName() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz"))

		assertThat key1, equalTo(key2)
		assertThat key1, not(equalTo(key3))
	}

	@Test
	void keyVariesWithParams() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]))
		def key4 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]))
		def key5 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]))

		assertThat key1, equalTo(key2)
		assertThat key1, not(equalTo(key4))
		assertThat key3, not(equalTo(key4))
		assertThat key4, equalTo(key5)
	}

	@Test
	void parameterOrderNotImportant() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [foo: "bar", id: "1"]))

		assertThat key1, equalTo(key2)
	}

	@Test
	void matchingSubsetOfParamsCreatesDifferentKey() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]))

		assertThat key1, not(equalTo(key2))
	}

	@Test
	void controllerAndActionParamsAreIgnored() {
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [controller: "foo", action: "bar"]))

		assertThat key1, equalTo(key2)
	}

	@Test
	void headAndGetRequetGenerateSameKey() {
		def headRequest = new MockHttpServletRequest("HEAD", "/foo/bar")
		def getRequest = new MockHttpServletRequest("GET", "/foo/bar")
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"], request: headRequest))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"], request: getRequest))

		assertThat key1, equalTo(key2)
	}

}

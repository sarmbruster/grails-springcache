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

import spock.lang.*
import grails.plugin.springcache.web.FilterContext
import org.springframework.mock.web.MockHttpServletRequest

class DefaultKeyGeneratorSpec extends Specification {

	KeyGenerator generator = new DefaultKeyGenerator()

	def "keys differ for different controller names"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key3 = generator.generateKey(new FilterContext(controllerName: "baz", actionName: "bar"))

		expect:
		key1 == key2
		key1 != key3
	}

	def "keys differ for different action names"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar"))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz"))

		expect:
		key1 == key2
		key1 != key3
	}

	def "keys differ for different request parameters"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]))
		def key4 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]))
		def key5 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "baz", params: [id: "2"]))

		expect:
		key1 == key2
		key1 != key4
		key3 != key4
		key4 == key5
	}

	def "array parameters are handled"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: ["1"] as String[]]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: ["1"] as String[]]))
		def key3 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: ["2"] as String[]]))
		def key4 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: ["1", ""] as String[]]))
		def key5 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: ["", "1", ""] as String[]]))

		expect:
		key1 == key2
		key1 != key3
		key4 != key5
	}

	def "parameter order is not important"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [foo: "bar", id: "1"]))

		expect:
		key1 == key2
	}

	def "keys differ when subsets of the parameters are different"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1", foo: "bar"]))

		expect:
		key1 != key2
	}

	def "controller and action parameters are ignored"() {
		given:
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [:]))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [controller: "foo", action: "bar"]))

		expect:
		key1 == key2
	}

	def "request method does not affect the key"() {
		given:
		def headRequest = new MockHttpServletRequest("HEAD", "/foo/bar")
		def getRequest = new MockHttpServletRequest("GET", "/foo/bar")
		def key1 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"], request: headRequest))
		def key2 = generator.generateKey(new FilterContext(controllerName: "foo", actionName: "bar", params: [id: "1"], request: getRequest))

		expect:
		key1 == key2
	}
}

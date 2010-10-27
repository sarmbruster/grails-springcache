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

import grails.plugin.springcache.key.KeyGenerator
import grails.plugin.springcache.web.ContentCacheParameters
import grails.util.GrailsWebUtil
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import spock.lang.Specification

class DefaultKeyGeneratorSpec extends Specification {

	KeyGenerator generator = new DefaultKeyGenerator()
	GrailsWebRequest webRequest = GrailsWebUtil.bindMockWebRequest()

	def "keys differ for different controller names"() {
		def key1 = generator.generateKey(cacheParams("foo", "bar"))
		def key2 = generator.generateKey(cacheParams("foo", "bar"))
		def key3 = generator.generateKey(cacheParams("baz", "bar"))

		expect:
		key1 == key2
		key1 != key3
	}

	def "keys differ for different action names"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar"))
		def key2 = generator.generateKey(cacheParams("foo", "bar"))
		def key3 = generator.generateKey(cacheParams("foo", "baz"))

		expect:
		key1 == key2
		key1 != key3
	}

	def "keys differ for different request parameters"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar", [:]))
		def key2 = generator.generateKey(cacheParams("foo", "bar", [:]))
		def key3 = generator.generateKey(cacheParams("foo", "bar", [id: "1"]))
		def key4 = generator.generateKey(cacheParams("foo", "baz", [id: "2"]))
		def key5 = generator.generateKey(cacheParams("foo", "baz", [id: "2"]))

		expect:
		key1 == key2
		key1 != key4
		key3 != key4
		key4 == key5
	}

	def "array parameters are handled"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar", [id: ["1"] as String[]]))
		def key2 = generator.generateKey(cacheParams("foo", "bar", [id: ["1"] as String[]]))
		def key3 = generator.generateKey(cacheParams("foo", "bar", [id: ["2"] as String[]]))
		def key4 = generator.generateKey(cacheParams("foo", "bar", [id: ["1", ""] as String[]]))
		def key5 = generator.generateKey(cacheParams("foo", "bar", [id: ["", "1", ""] as String[]]))

		expect:
		key1 == key2
		key1 != key3
		key4 != key5
	}

	def "parameter order is not important"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar", [id: "1", foo: "bar"]))
		def key2 = generator.generateKey(cacheParams("foo", "bar", [foo: "bar", id: "1"]))

		expect:
		key1 == key2
	}

	def "keys differ when subsets of the parameters are different"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar", [id: "1"]))
		def key2 = generator.generateKey(cacheParams("foo", "bar", [id: "1", foo: "bar"]))

		expect:
		key1 != key2
	}

	def "controller and action parameters are ignored"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar", [:]))
		def key2 = generator.generateKey(cacheParams("foo", "bar", [controller: "foo", action: "bar"]))

		expect:
		key1 == key2
	}

	def "request method does not affect the key"() {
		given:
		def key1 = generator.generateKey(cacheParams("foo", "bar", [id: "1"], "HEAD"))
		def key2 = generator.generateKey(cacheParams("foo", "bar", [id: "1"], "GET"))

		expect:
		key1 == key2
	}

	static ContentCacheParameters cacheParams(String controllerName, String actionName, Map params = [:], String method = "GET") {
		def webRequest = GrailsWebUtil.bindMockWebRequest()
		webRequest.controllerName = controllerName
		webRequest.actionName = actionName
		webRequest.currentRequest.parameters = params
		webRequest.currentRequest.method = method
		new ContentCacheParameters(webRequest)
	}
}

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

import grails.plugin.spock.UnitSpec
import grails.plugin.springcache.key.KeyGenerator
import grails.plugin.springcache.web.ContentCacheParameters
import grails.util.GrailsWebUtil

class MimeTypeAwareKeyGeneratorSpec extends UnitSpec {

	KeyGenerator generator = new MimeTypeAwareKeyGenerator()

	def "keys differ for different request content types"() {
		given:
		def key1 = generator.generateKey(cacheParams("html"))
		def key2 = generator.generateKey(cacheParams("html"))
		def key3 = generator.generateKey(cacheParams("xml"))

		expect:
		key1 == key2
		key1 != key3
	}

	def "content type 'all' is ignored"() {
		given:
		def key1 = generator.generateKey(cacheParams("all"))
		def key2 = generator.generateKey(cacheParams(null))

		expect:
		key1 == key2
	}

	static ContentCacheParameters cacheParams(String format) {
		def webRequest = GrailsWebUtil.bindMockWebRequest()
		webRequest.controllerName = "foo"
		webRequest.actionName = "bar"
		webRequest.currentRequest.metaClass.getFormat = {-> format }
		new ContentCacheParameters(webRequest)
	}

}

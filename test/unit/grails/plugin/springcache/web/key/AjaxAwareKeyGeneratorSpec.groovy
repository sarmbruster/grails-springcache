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
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import static grails.plugin.springcache.web.key.AjaxAwareKeyGenerator.X_REQUESTED_WITH

class AjaxAwareKeyGeneratorSpec extends UnitSpec {

	KeyGenerator generator = new AjaxAwareKeyGenerator()

	def setup() {
		registerMetaClass HttpServletRequest
		// as per ServletsGrailsPlugin
		HttpServletRequest.metaClass.isXhr = { ->
			delegate.getHeader(X_REQUESTED_WITH) != null
		}
	}

	def "keys differ depending on whether request has xhr header or not"() {
		given:
		def key1 = generator.generateKey(ajaxRequestParameters)
		def key2 = generator.generateKey(ajaxRequestParameters)
		def key3 = generator.generateKey(nonAjaxRequestParameters)

		expect:
		key1 == key2
		key1 != key3
	}

	static final ContentCacheParameters getNonAjaxRequestParameters() {
		new ContentCacheParameters(bindMockRequest())
	}

	static final ContentCacheParameters getAjaxRequestParameters() {
		def request = bindMockRequest()
		request.currentRequest.addHeader(X_REQUESTED_WITH, "true")
		new ContentCacheParameters(request)
	}

	private static GrailsWebRequest bindMockRequest() {
		def webRequest = GrailsWebUtil.bindMockWebRequest()
		webRequest.controllerName = "foo"
		webRequest.actionName = "bar"
		return webRequest
	}

}

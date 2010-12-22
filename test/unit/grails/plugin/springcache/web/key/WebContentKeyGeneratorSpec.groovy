/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web.key

import grails.plugin.spock.UnitSpec
import grails.plugin.springcache.web.ContentCacheParameters
import grails.util.GrailsWebUtil
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import static grails.plugin.springcache.web.key.WebContentKeyGenerator.X_REQUESTED_WITH
import static org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.CONTENT_FORMAT

class WebContentKeyGeneratorSpec extends UnitSpec {

	def generator = new WebContentKeyGenerator()

	def setup() {
		registerMetaClass HttpServletRequest
		HttpServletRequest.metaClass.getFormat = {->
			delegate.getAttribute(CONTENT_FORMAT)
		}
	}

	def "by default xhr header is ignored"() {
		given:
		def ajaxRequest = new RequestBuilder().headers((X_REQUESTED_WITH): "XMLHttpRequest").toCacheParams()
		def nonAjaxRequest = new RequestBuilder().toCacheParams()

		when:
		def key1 = generator.generateKey(ajaxRequest)
		def key2 = generator.generateKey(nonAjaxRequest)

		then:
		key1 == key2
	}

	def "by default content type is ignored"() {
		given:
		def xmlRequest = new RequestBuilder().format("xml").toCacheParams()
		def jsonRequest = new RequestBuilder().format("json").toCacheParams()

		when:
		def key1 = generator.generateKey(xmlRequest)
		def key2 = generator.generateKey(jsonRequest)

		then:
		key1 == key2
	}

	def "by default request method is ignored"() {
		given:
		def getRequest = new RequestBuilder().method("GET").toCacheParams()
		def postRequest = new RequestBuilder().method("POST").toCacheParams()

		when:
		def key1 = generator.generateKey(getRequest)
		def key2 = generator.generateKey(postRequest)

		then:
		key1 == key2
	}

	def "keys can differ depending on whether request is ajax or not"() {
		given:
		def ajaxRequest = new RequestBuilder().headers((X_REQUESTED_WITH): "XMLHttpRequest").toCacheParams()
		def nonAjaxRequest = new RequestBuilder().toCacheParams()

		and:
		generator.ajax = true

		when:
		def key1 = generator.generateKey(ajaxRequest)
		def key2 = generator.generateKey(ajaxRequest)
		def key3 = generator.generateKey(nonAjaxRequest)

		then:
		key1 == key2
		key1 != key3
	}

	def "keys can differ depending on requested content type"() {
		given:
		def xmlRequest = new RequestBuilder().format("xml").toCacheParams()
		def jsonRequest = new RequestBuilder().format("json").toCacheParams()

		and:
		generator.contentType = true

		when:
		def key1 = generator.generateKey(xmlRequest)
		def key2 = generator.generateKey(xmlRequest)
		def key3 = generator.generateKey(jsonRequest)

		then:
		key1 == key2
		key1 != key3
	}

	def "'all' format is ignored"() {
		given:
		def request1 = new RequestBuilder().format("all").toCacheParams()
		def request2 = new RequestBuilder().toCacheParams()

		and:
		generator.contentType = true

		when:
		def key1 = generator.generateKey(request1)
		def key2 = generator.generateKey(request2)

		then:
		key1 == key2
	}

	def "keys can differ depending on request method"() {
		given:
		def getRequest = new RequestBuilder().method("GET").toCacheParams()
		def postRequest = new RequestBuilder().method("POST").toCacheParams()

		and:
		generator.requestMethod = true

		when:
		def key1 = generator.generateKey(getRequest)
		def key2 = generator.generateKey(getRequest)
		def key3 = generator.generateKey(postRequest)

		then:
		key1 == key2
		key1 != key3
	}

	def "get and head requests should generate the same key"() {
		given:
		def getRequest = new RequestBuilder().method("GET").toCacheParams()
		def headRequest = new RequestBuilder().method("HEAD").toCacheParams()

		and:
		generator.requestMethod = true

		when:
		def key1 = generator.generateKey(getRequest)
		def key2 = generator.generateKey(headRequest)

		then:
		key1 == key2
	}

	static ContentCacheParameters cacheParams(String controllerName, String actionName, Map params = [:], String method = "GET", Map headers = [:]) {
		def webRequest = GrailsWebUtil.bindMockWebRequest()
		webRequest.controllerName = controllerName
		webRequest.actionName = actionName
		webRequest.currentRequest.parameters = params
		webRequest.currentRequest.method = method
		headers.each { k, v ->
			webRequest.currentRequest.addHeader(k, v)
		}
		new ContentCacheParameters(webRequest)
	}

}

class RequestBuilder {

	private GrailsWebRequest webRequest

	RequestBuilder() {
		webRequest = GrailsWebUtil.bindMockWebRequest()
		webRequest.controllerName = "a"
		webRequest.actionName = "index"
	}

	RequestBuilder controller(String controllerName) {
		webRequest.controllerName = controllerName
		this
	}

	RequestBuilder action(String actionName) {
		webRequest.actionName = actionName
		this
	}

	RequestBuilder params(Map<String, String> params) {
		webRequest.currentRequest.parameters = params
		this
	}

	RequestBuilder method(String method) {
		webRequest.currentRequest.method = method
		this
	}

	RequestBuilder headers(Map<String, String> headers) {
		headers.each { k, v ->
			webRequest.currentRequest.addHeader(k, v)
		}
		this
	}

	RequestBuilder format(String format) {
		webRequest.currentRequest.setAttribute(CONTENT_FORMAT, format)
		this
	}

	ContentCacheParameters toCacheParams() {
		new ContentCacheParameters(webRequest)
	}

}

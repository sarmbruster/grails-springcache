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
package grails.plugin.springcache.web

import grails.plugin.springcache.annotations.Cacheable
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import grails.plugin.springcache.web.key.*
import org.codehaus.groovy.grails.commons.*
import static org.hamcrest.Matchers.*
import spock.lang.*
import static spock.util.matcher.MatcherSupport.that

class FilterContextSpec extends Specification {

	GrailsWebRequest request = Mock()

	def setup() {
		// set up the controller as an artefact
		def application = Mock(GrailsApplication)
		[cachedTest: CachedTestController, uncachedTest: UncachedTestController, restfulTest: RestfulTestController].each { name, controllerClass ->
			def artefact = new DefaultGrailsControllerClass(controllerClass)
			application.getArtefactByLogicalPropertyName("Controller", name) >> artefact
		}
		ApplicationHolder.application = application

		// put the mock request in the evil static holder
		RequestContextHolder.requestAttributes = request
	}

	@Unroll
	def "controller and action are identified based on the request context"() {
		given: "there is a request context"
		request.controllerName >> controllerName
		request.actionName >> actionName
		def context = new FilterContext()

		expect:
		context.controllerArtefact?.clazz == expectedController
		!expectedAction || context.actionClosure != null
		
		where:
		controllerName | actionName | expectedController   | expectedAction
		null           | null       | null                 | false
		"cachedTest"   | "list1"    | CachedTestController | true
		"cachedTest"   | null       | CachedTestController | true // action is controller's default action
		"cachedTest"   | "blah"     | CachedTestController | false
	}

	@Unroll("isRequestCacheable returns #shouldBeCacheable when controller is '#controllerName' and action is '#actionName'")
	def "a request is considered cachable if there is an annotation on the controller or action"() {
		given: "there is a request context"
		request.controllerName >> controllerName
		request.actionName >> actionName
		def context = new FilterContext()

		expect:
		context.isRequestCacheable() == shouldBeCacheable

		where:
		controllerName | actionName | shouldBeCacheable
		null           | null       | false
		"uncachedTest" | null       | false
		"uncachedTest" | "index"    | false
		"cachedTest"   | "list1"    | true
		"cachedTest"   | "list2"    | true
		"cachedTest"   | "list3"    | true
		"cachedTest"   | null       | true
	    "cachedTest"   | "blah"     | true
	}

	@Unroll("cache name is '#expectedCacheName' when controller is '#controllerName' and action is '#actionName'")
	def "the cache name is identified based on the annotation on the controller or action"() {
		given: "there is a request context"
		request.controllerName >> controllerName
		request.actionName >> actionName
		def context = new FilterContext()

		expect:
		context.cacheName == expectedCacheName

		where:
		controllerName | actionName | expectedCacheName
		null           | null       | null
		"uncachedTest" | null       | null
		"uncachedTest" | "index"    | null
		"cachedTest"   | "index"    | "testControllerCache"
		"cachedTest"   | "list1"    | "listActionCache"
		"cachedTest"   | "list2"    | "listActionCache"
		"cachedTest"   | "list3"    | "listActionCache"
		"cachedTest"   | null       | "testControllerCache"
	    "cachedTest"   | "blah"     | "testControllerCache"
	}

	@Unroll("key generator is #keyGeneratorMatcher when controller is '#controllerName' and action is '#actionName'")
	def "a key generator is created if an annotation is present on the controller or action"() {
		given: "there is a request context"
		request.controllerName >> controllerName
		request.actionName >> actionName
		def context = new FilterContext()

		expect:
		that context.keyGenerator, keyGeneratorMatcher

		where:
		controllerName | actionName | keyGeneratorMatcher
		null           | null       | nullValue()
		"uncachedTest" | null       | nullValue()
		"uncachedTest" | "index"    | nullValue()
		"cachedTest"   | "index"    | instanceOf(DefaultKeyGenerator)
		"cachedTest"   | "list1"    | instanceOf(DefaultKeyGenerator)
		"cachedTest"   | "list2"    | instanceOf(DefaultKeyGenerator)
		"cachedTest"   | "list3"    | instanceOf(MimeTypeAwareKeyGenerator)
		"cachedTest"   | null       | instanceOf(DefaultKeyGenerator)
	    "cachedTest"   | "blah"     | instanceOf(DefaultKeyGenerator)
		"restfulTest"  | "list"     | instanceOf(MimeTypeAwareKeyGenerator)
	}
}

@Cacheable("testControllerCache")
class CachedTestController {

	def index = {}

	@Cacheable("listActionCache")
	def list1 = {}

	@Cacheable(cache = "listActionCache")
	def list2 = {}

	@Cacheable(cache = "listActionCache", keyGeneratorType = MimeTypeAwareKeyGenerator)
	def list3 = {}
}

class UncachedTestController {

	def index = {}

}

@Cacheable(cache = "testControllerCache", keyGeneratorType = MimeTypeAwareKeyGenerator)
class RestfulTestController {

	def list = {}

}
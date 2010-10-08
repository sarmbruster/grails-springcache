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
import org.codehaus.groovy.grails.commons.*
import spock.lang.*
import static org.hamcrest.CoreMatchers.*
import grails.plugin.springcache.web.key.MimeTypeAwareKeyGenerator
import grails.plugin.springcache.annotations.KeyGeneratorType

class FilterContextSpec extends Specification {

	GrailsWebRequest request = Mock()

	def setup() {
		// set up the controller as an artefact
		def artefact = new DefaultGrailsControllerClass(TestController)
		def application = Mock(GrailsApplication)
		application.getArtefactByLogicalPropertyName("Controller", "test") >> artefact
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
		controllerName | actionName | expectedController | expectedAction
		null           | null       | null               | false
		"test"         | "list"     | TestController     | true
		"test"         | null       | TestController     | true // action is controller's default action
		"test"         | "blah"     | TestController     | false
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
		"test"         | "list"     | true
		"test"         | null       | true
	    "test"         | "blah"     | true
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
		"test"         | "list"     | "listActionCache"
		"test"         | null       | "testControllerCache"
	    "test"         | "blah"     | "testControllerCache"
	}

	@Unroll("key generator is #keyGeneratorMatcher when controller is '#controllerName' and action is '#actionName'")
	def "a key generator is created if an annotation is present on the controller or action"() {
		given: "there is a request context"
		request.controllerName >> controllerName
		request.actionName >> actionName
		def context = new FilterContext()

		expect:
		keyGeneratorMatcher.matches(context.keyGenerator)

		where:
		controllerName | actionName | keyGeneratorMatcher
		null           | null       | nullValue()
		"test"         | "list"     | instanceOf(MimeTypeAwareKeyGenerator)
		"test"         | null       | nullValue()
	    "test"         | "blah"     | nullValue()
		// TODO: test for @KeyGeneratorType at controller level
	}
}

@Cacheable("testControllerCache")
class TestController {

	def index = {}

	@Cacheable("listActionCache")
	@KeyGeneratorType(MimeTypeAwareKeyGenerator)
	def list = {}
}
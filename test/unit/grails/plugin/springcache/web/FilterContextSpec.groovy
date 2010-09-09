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

import spock.lang.*
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class FilterContextSpec extends Specification {
	
	@Unroll
	def "controller and action are identified based on the request context"() {
		given: "the controller artifact exists in the grails application"
		def artefact = new DefaultGrailsControllerClass(TestController)
		def application = Mock(GrailsApplication)
		application.getArtefactByLogicalPropertyName("Controller", "test") >> artefact
		ApplicationHolder.application = application
		
		and: "there is a request context"
		def request = Mock(GrailsWebRequest)
		request.controllerName >> controllerName
		request.actionName >> actionName
		RequestContextHolder.requestAttributes = request

		expect:
		def context = new FilterContext()
		context.controllerArtefact?.clazz == expectedController
		!expectedAction || context.actionClosure != null
		
		where:
		controllerName | actionName | expectedController | expectedAction
		null           | null       | null               | false
		"test"         | "list"     | TestController     | true
		"test"         | null       | TestController     | true // action is controller's default action
		"test"         | "blah"     | TestController     | false
	}

}

class TestController {
	def index = {}
	def list = {}
}
package grails.plugin.springcache.web

import static org.hamcrest.Matchers.*

import grails.plugin.spock.UnitSpec
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import spock.lang.Unroll
import org.codehaus.groovy.grails.commons.*
import static spock.util.matcher.MatcherSupport.that

class ContentCacheParametersSpec extends UnitSpec {

	def setup() {
		// set up the controllers as artefacts
		def application = Mock(GrailsApplication)
		[TestController].each { controllerClass ->
			def name = GrailsNameUtils.getLogicalPropertyName(controllerClass.name, "Controller")
			def artefact = new DefaultGrailsControllerClass(controllerClass)
			application.getArtefactByLogicalPropertyName("Controller", name) >> artefact
		}
		ApplicationHolder.application = application
	}

	@Unroll("controller is #expectedController and action is #expectedAction when controllerName is '#controllerName' and actionName is '#actionName'")
	def "controller and action are identified based on the request context"() {
		given:
		def webRequest = Mock(GrailsWebRequest)
		webRequest.controllerName >> controllerName
		webRequest.actionName >> actionName

		when:
		def cacheParameters = new ContentCacheParameters(webRequest)

		then:
		cacheParameters.controller?.clazz == expectedController
		that cacheParameters.action, expectedAction

		where:
		controllerName | actionName | expectedController | expectedAction
		null           | null       | null               | nullValue()
		"test"         | "index"    | TestController     | hasProperty("name", equalTo("index"))
		"test"         | "list"     | TestController     | hasProperty("name", equalTo("list"))
		"test"         | null       | TestController     | hasProperty("name", equalTo("index"))
		"test"         | "blah"     | TestController     | nullValue()
	}
}

class TestController {

	def index = {}
	def list = {}

}

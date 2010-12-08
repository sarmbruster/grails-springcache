package grails.plugin.springcache.web

import java.lang.reflect.Field
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.web.servlet.mvc.*

class ContentCacheParameters {

	private final GrailsWebRequest grailsWebRequest
	@Lazy GrailsControllerClass controller = initController()
	@Lazy Field action = initAction()

	ContentCacheParameters(GrailsWebRequest grailsWebRequest) {
		this.grailsWebRequest = grailsWebRequest
	}

	String getControllerName() {
		grailsWebRequest.controllerName
	}

	String getActionName() {
		grailsWebRequest.actionName ?: controller?.defaultAction
	}

	GrailsParameterMap getParams() {
		grailsWebRequest.params
	}

	HttpServletRequest getRequest() {
		grailsWebRequest.currentRequest
	}

	Class getControllerClass() {
		controller?.clazz
	}

	private GrailsControllerClass initController() {
		ApplicationHolder.application.getArtefactByLogicalPropertyName("Controller", controllerName)
	}

	private Field initAction() {
		try {
			controller?.clazz?.getDeclaredField(getActionName())
		} catch (NoSuchFieldException e) {
			// this can happen with dynamic scaffolded actions
			null
		}
	}

	String toString() {
		def buffer = new StringBuilder("[")
		buffer << "controller=" << controllerName
		if (controller == null) buffer << "?"
		buffer << ", action=" << actionName
		if (action == null) buffer << "?"
		buffer << "]"
		return buffer.toString()
	}

}

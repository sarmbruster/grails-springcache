package grails.plugin.springcache.web

import grails.plugin.springcache.CacheParameters
import java.lang.reflect.Field
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsControllerClass

class ContentCacheParameters implements CacheParameters {

	final String controllerName
	final String actionName
	final Map params
	final HttpServletRequest request
	@Lazy GrailsControllerClass controller = initController()
	@Lazy Field action = initAction()

	ContentCacheParameters(requestAttributes) {
		controllerName = requestAttributes.controllerName
		actionName = requestAttributes.actionName
		params = requestAttributes.params
		request = requestAttributes.request
	}

	String getActionName() {
		actionName ?: controller?.defaultAction
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

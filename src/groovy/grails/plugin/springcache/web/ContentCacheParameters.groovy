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

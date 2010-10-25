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
import grails.plugin.springcache.key.KeyGenerator
import java.lang.annotation.Annotation
import java.lang.reflect.Field
import javax.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.*

class FilterContext {

	String controllerName
	String actionName
	Map params
	HttpServletRequest request

	@Lazy String cacheName = resolveCacheName()
	@Lazy KeyGenerator keyGenerator = cacheable?.keyGeneratorType()?.newInstance()
	@Lazy private Cacheable cacheable = getAnnotation(Cacheable)

	FilterContext() {
		request = RequestContextHolder.requestAttributes?.request
		controllerName = RequestContextHolder.requestAttributes?.controllerName
		actionName = RequestContextHolder.requestAttributes?.actionName ?: controllerArtefact?.defaultAction
		params = RequestContextHolder.requestAttributes?.parameterMap?.asImmutable()
	}

	@Lazy GrailsControllerClass controllerArtefact = {
		controllerName ? ApplicationHolder.application.getArtefactByLogicalPropertyName("Controller", controllerName) : null
	}()

	@Lazy Field actionClosure = {
		try {
			return actionName ? controllerArtefact?.clazz?.getDeclaredField(actionName) : null
		} catch (NoSuchFieldException e) {
			// happens with dynamic scaffolded controllers
			return null
		}
	}()

	boolean isRequestCacheable() {
		cacheable != null
	}

	private String resolveCacheName() {
		if (isRequestCacheable()) {
			return cacheable.cache() ?: cacheable.value()
		} else {
			return null
		}
	}

	private Annotation getAnnotation(Class type) {
		// first look on the action, then the controller class
		return actionClosure?.getAnnotation(type) ?: controllerArtefact?.clazz?.getAnnotation(type)
	}

	String toString() {
		def buffer = new StringBuilder("[")
		buffer << "controller=" << controllerName
		if (controllerArtefact == null) buffer << "?"
		buffer << ", action=" << actionName
		if (actionClosure == null) buffer << "?"
		buffer << "]"
		return buffer.toString()
	}

}
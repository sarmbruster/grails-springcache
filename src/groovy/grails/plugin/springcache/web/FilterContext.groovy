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
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.*
import grails.plugin.springcache.annotations.CacheFlush

class FilterContext {

	final ContentCacheParameters cacheParameters

	private final GrailsControllerClass controllerArtefact
	private final Field actionClosure

	@Lazy String cacheName = resolveCacheName()
	@Lazy KeyGenerator keyGenerator = cacheable?.keyGeneratorType()?.newInstance()
	@Lazy private Cacheable cacheable = getAnnotation(Cacheable)
	@Lazy private CacheFlush cacheFlush = getAnnotation(CacheFlush)

	FilterContext() {
		GrailsWebRequest requestAttributes = RequestContextHolder.requestAttributes
		
		def request = requestAttributes?.request
		def params = requestAttributes?.parameterMap?.asImmutable()

		def controllerName = requestAttributes?.controllerName
		controllerArtefact = getControllerArtefact(controllerName)

		def actionName = requestAttributes?.actionName ?: controllerArtefact?.defaultAction
		actionClosure = getActionClosure(controllerName, actionName)

		cacheParameters = new ContentCacheParameters(request: request, controllerName: controllerName, actionName: actionName, params: params)
	}

	boolean shouldCache() {
		cacheable != null
	}

	boolean shouldFlush() {
		cacheFlush != null
	}

	private String resolveCacheName() {
		if (shouldCache()) {
			return cacheable.cache() ?: cacheable.value()
		} else {
			return null
		}
	}

	private Annotation getAnnotation(Class type) {
		// first look on the action, then the controller class
		return actionClosure?.getAnnotation(type) ?: controllerArtefact?.clazz?.getAnnotation(type)
	}

	private GrailsControllerClass getControllerArtefact(String controllerName) {
		controllerName ? ApplicationHolder.application.getArtefactByLogicalPropertyName("Controller", controllerName) : null
	}

	private Field getActionClosure(String controllerName, String actionName) {
		try {
			return actionName ? getControllerArtefact(controllerName)?.clazz?.getDeclaredField(actionName) : null
		} catch (NoSuchFieldException e) {
			// happens with dynamic scaffolded controllers
			return null
		}
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
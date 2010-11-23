/*
 * Copyright 2010 Luke Daley
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
package grails.plugin.springcache.taglib

import grails.plugin.springcache.annotations.Cacheable
import org.slf4j.LoggerFactory
import org.codehaus.groovy.grails.commons.*

/**
 * Inspects tag lib instances, looking for tags annotated with cacheable. Any that are found, are
 * replaced with a CachingTag wrapper that implements the caching. 
 * 
 * To be clear, we actually set a different closure to the field on the instance.
 */
class CachingTagLibDecorator {

	static private final log = LoggerFactory.getLogger(CachingTagLibDecorator)
	
	final springcacheService
	
	CachingTagLibDecorator(springcacheService) {
		this.springcacheService = springcacheService
	}
	
	void decorate(GrailsTagLibClass tagLibClass, tagLib) {
		def clazz = tagLibClass.clazz
		def namespace = GrailsClassUtils.getStaticPropertyValue(clazz, "namespace") ?: "g"
		
		tagLibClass.tags.each { tagName ->
			def field = getTagField(tagName, clazz)
			if (field) {
				def annotation = field.getAnnotation(Cacheable)
				if (annotation) {
					def tag = getTagImplementation(tagName, tagLib)
					if (log.debugEnabled) {
						log.debug("decorating tag '$tagName' of $clazz.name with caching")
					}
					
					tagLib."$tagName" = new CachingTag(namespace, tagName, tag, annotation, springcacheService)
				}
			}
		}
	}
	
	protected getTagField(String name, Class tagLibClass) {
		try {
			tagLibClass.getDeclaredField(name)
		} catch (NoSuchFieldException e) {
			// To support tag inheritance we could search the superclass here
			null
		}
	}
	
	protected Closure getTagImplementation(String name, tagLib) {
		tagLib."$name"
	}
}
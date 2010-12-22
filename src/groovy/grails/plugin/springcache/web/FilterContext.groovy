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

import grails.plugin.springcache.CacheResolver
import grails.plugin.springcache.key.KeyGenerator
import grails.plugin.springcache.web.key.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder
import grails.plugin.springcache.annotations.*

class FilterContext {

	final ContentCacheParameters cacheParameters

	@Lazy private Cacheable cacheableAnnotation = findAnnotation(Cacheable)
	@Lazy private CacheFlush cacheFlushAnnotation = findAnnotation(CacheFlush)

	FilterContext() {
		GrailsWebRequest requestAttributes = RequestContextHolder.requestAttributes
		cacheParameters = new ContentCacheParameters(requestAttributes)
	}

	boolean shouldCache() {
		cacheableAnnotation != null
	}

	boolean shouldFlush() {
		cacheFlushAnnotation != null
	}

	List<String> getCacheNames() {
		if (!shouldFlush()) throw new IllegalStateException("Only supported on flushing requests")
		CacheResolver cacheResolver = getBean(cacheFlushAnnotation.cacheResolver())
		baseCacheNames.collect {
			cacheResolver.resolveCacheName(it)
		}
	}

	String getCacheName() {
		if (!shouldCache()) throw new IllegalStateException("Only supported on caching requests")
		CacheResolver cacheResolver = getBean(cacheableAnnotation.cacheResolver())
		cacheResolver.resolveCacheName(baseCacheName)
	}

	KeyGenerator getKeyGenerator() {
		if (!shouldCache()) throw new IllegalStateException("Only supported on caching requests")
		getBean(cacheableAnnotation.keyGenerator())
	}

	private getBaseCacheName() {
		if (!shouldCache()) throw new IllegalStateException("Only supported on caching requests")
		cacheableAnnotation.cache() ?: cacheableAnnotation.value()
	}

	private String[] getBaseCacheNames() {
		if (!shouldFlush()) throw new IllegalStateException("Only supported on flushing requests")
		cacheFlushAnnotation.caches() ?: cacheFlushAnnotation.value()
	}

	private <T> T findAnnotation(Class<T> annotationType) {
		cacheParameters.with {
			action?.getAnnotation(annotationType) ?: controllerClass?.getAnnotation(annotationType)
		}
	}

	private <T> T getBean(String cacheResolverBeanName) {
		return ApplicationHolder.application.mainContext[cacheResolverBeanName]
	}

}
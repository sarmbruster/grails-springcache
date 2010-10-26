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

	@Lazy String cacheName = resolveCacheName()
	@Lazy KeyGenerator keyGenerator = cacheable?.keyGeneratorType()?.newInstance()
	@Lazy private Cacheable cacheable = getAnnotation(Cacheable)
	@Lazy private CacheFlush cacheFlush = getAnnotation(CacheFlush)

	FilterContext() {
		GrailsWebRequest requestAttributes = RequestContextHolder.requestAttributes
		cacheParameters = new ContentCacheParameters(requestAttributes)
	}

	boolean shouldCache() {
	}

	boolean shouldFlush() {
	}

	List<String> getCacheNames() {
	}

	String getCacheName() {
	}

	KeyGenerator getKeyGenerator() {

	}
}
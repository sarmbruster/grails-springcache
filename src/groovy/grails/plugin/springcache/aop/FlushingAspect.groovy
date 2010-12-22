/*
 * Copyright 2009 Rob Fletcher
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
package grails.plugin.springcache.aop

import grails.plugin.springcache.SpringcacheService
import grails.plugin.springcache.annotations.CacheFlush
import org.aspectj.lang.annotation.*
import org.slf4j.*
import grails.plugin.springcache.CacheResolver
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

@Aspect
public class FlushingAspect implements ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(FlushingAspect.class)

	SpringcacheService springcacheService
	ApplicationContext applicationContext

	@After("@annotation(cacheFlush)")
	void flushCaches(final CacheFlush cacheFlush) {
		if (log.isDebugEnabled()) log.debug "Flushing cache(s): ${cacheFlush.value().join(', ')}"
		springcacheService.flush(resolveCacheName(cacheFlush))
	}

	private List<String> resolveCacheName(CacheFlush cacheFlush) {
		CacheResolver resolver = applicationContext[cacheFlush.cacheResolver()]
		def baseNames = cacheFlush.caches() ?: cacheFlush.value()
		baseNames.collect {
			resolver.resolveCacheName(it)
		}
	}

}

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

import grails.plugin.springcache.annotations.Cacheable
import org.aspectj.lang.ProceedingJoinPoint
import grails.plugin.springcache.*
import org.aspectj.lang.annotation.*
import org.slf4j.*
import org.springframework.context.*

@Aspect
class CachingAspect implements ApplicationContextAware {

	private final Logger log = LoggerFactory.getLogger(CachingAspect.class)

	SpringcacheService springcacheService
	ApplicationContext applicationContext

	@Around("@annotation(cacheable)")
	Object invokeCachedMethod(ProceedingJoinPoint pjp, Cacheable cacheable) {
		if (log.isDebugEnabled()) log.debug "Intercepted ${pjp.toLongString()}"
		def cacheName = resolveCacheName(cacheable)
		def key = CacheKey.generate(pjp.target, pjp.signature.name, pjp.args)
		return springcacheService.doWithCache(cacheName, key) {
			pjp.proceed()
		}
	}

	private String resolveCacheName(Cacheable cacheable) {
		def baseName = cacheable.cache() ?: cacheable.value()
		CacheResolver resolver = applicationContext[cacheable.cacheResolver()]
		resolver.resolveCacheName(baseName)
	}

}
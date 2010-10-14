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
import org.slf4j.LoggerFactory
import org.springframework.cache.ehcache.*
import grails.plugin.springcache.aop.CachingAspect
import grails.plugin.springcache.aop.FlushingAspect
import grails.plugin.springcache.web.GrailsFragmentCachingFilter
import org.springframework.web.filter.DelegatingFilterProxy
import grails.plugin.springcache.web.key.DefaultKeyGenerator
import org.codehaus.groovy.grails.commons.GrailsApplication

class SpringcacheGrailsPlugin {

	def version = "1.3-SNAPSHOT"
	def grailsVersion = "1.2.0 > *"
	def dependsOn = [:]
	def pluginExcludes = [
			"grails-app/views/**",
			"web-app/**",
			"**/.gitignore",
			"grails-app/services/grails/plugin/springcache/test/**",
	]

	def author = "Grails Plugin Collective"
	def authorEmail = "grails.plugin.collective@gmail.com"
	def title = "Spring Cache Plugin"
	def description = "Provides annotation-driven caching of service methods and page fragments."
	def documentation = "http://gpc.github.com/grails-springcache"

	def doWithWebDescriptor = {xml ->
		if (isEnabled(application)) {
			def filters = xml.filter
			def lastFilter = filters[filters.size() - 1]
			lastFilter + {
				filter {
					"filter-name" "springcacheContentCache"
					"filter-class" DelegatingFilterProxy.name
					"init-param" {
						"param-name" "targetBeanName"
						"param-value" "springcacheFilter"
					}
					"init-param" {
						"param-name" "targetFilterLifecycle"
						"param-value" "true"
					}
				}
			}

			def filterMappings = xml."filter-mapping"
			def lastMapping = filterMappings[filterMappings.size() - 1]
			lastMapping + {
				"filter-mapping" {
					"filter-name" "springcacheContentCache"
					"url-pattern" "*.dispatch"
					dispatcher "FORWARD"
					dispatcher "INCLUDE"
				}
			}
		}
	}

	def doWithSpring = {
		if (!isEnabled(application)) {
			log.warn "Springcache plugin is disabled"
		} else {
			if (application.config.grails.spring.disable.aspectj.autoweaving) {
				log.warn "Service method caching is not compatible with the config setting 'grails.spring.disable.aspectj.autoweaving = false'"
			}

			springcacheCacheManager(EhCacheManagerFactoryBean) {
				cacheManagerName = "Springcache Plugin Cache Manager"
			}

			springcacheDefaultCache(EhCacheFactoryBean) { bean ->
				bean."abstract" = true
				cacheManager = ref("springcacheCacheManager")
				application.config.springcache.defaults.each {
					bean.setPropertyValue it.key, it.value
				}
			}

			application.config.springcache.caches.each {String name, ConfigObject cacheConfig ->
				"$name"(EhCacheFactoryBean) {bean ->
					bean.parent = springcacheDefaultCache
					cacheName = name
					cacheConfig.each {
						bean.setPropertyValue it.key, it.value
					}
				}
			}

			springcacheCachingAspect(CachingAspect) {
				springcacheService = ref("springcacheService")
			}

			springcacheFlushingAspect(FlushingAspect) {
				springcacheService = ref("springcacheService")
			}

			springcacheFilter(GrailsFragmentCachingFilter) {
				springcacheService = ref("springcacheService")
				cacheManager = ref("springcacheCacheManager")
				defaultKeyGenerator = new DefaultKeyGenerator()
			}
		}
	}

	def doWithDynamicMethods = {ctx ->
	}

	def doWithApplicationContext = {applicationContext ->
	}

	def onChange = {event ->
	}

	def onConfigChange = {event ->
	}

	private static final log = LoggerFactory.getLogger("grails.plugin.springcache.SpringcacheGrailsPlugin")

	private boolean isEnabled(GrailsApplication application) {
		application.config.with {
			(springcache.enabled == null || springcache.enabled != false) && !springcache.disabled 
		}
	}

}


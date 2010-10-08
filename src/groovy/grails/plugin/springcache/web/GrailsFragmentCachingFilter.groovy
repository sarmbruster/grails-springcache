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

import grails.plugin.springcache.SpringcacheService
import java.lang.annotation.Annotation
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import org.codehaus.groovy.grails.web.util.WebUtils
import org.slf4j.LoggerFactory
import grails.plugin.springcache.annotations.*
import grails.plugin.springcache.web.key.KeyGenerator
import javax.servlet.*
import javax.servlet.http.*
import net.sf.ehcache.*
import net.sf.ehcache.constructs.web.*
import net.sf.ehcache.constructs.web.filter.PageFragmentCachingFilter
import org.codehaus.groovy.grails.web.servlet.*

class GrailsFragmentCachingFilter extends PageFragmentCachingFilter {

	private final log = LoggerFactory.getLogger(getClass())
	private final timingLog = LoggerFactory.getLogger("${getClass().name}.TIMINGS")
	SpringcacheService springcacheService
	CacheManager cacheManager
	KeyGenerator defaultKeyGenerator

	private ThreadLocal<FilterContext> contextHolder = new ThreadLocal<FilterContext>()

	/**
	 * Overrides doInit in CachingFilter to be a no-op. The superclass initializes a single cache that is used for all
	 * intercepted requests but we will select a cache at runtime based on the target controller/action.
	 */
	@Override
	void doInit(FilterConfig filterConfig) {
		// don't do anything - we need to get caches differently for each request
	}

	/**
	 * Overrides doFilter in PageFragmentCachingFilter to handle flushing and caching behaviour selectively depending
	 * on annotations on target controller.
	 */
	@Override protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		initContext()
		try {
			if (handleFlush(request)) {
				chain.doFilter(request, response)
			} else if (context.isRequestCacheable()) {
				logRequestDetails(request, context, "Caching enabled for request")
				super.doFilter(request, response, chain)
			} else {
				log.debug "No cacheable annotation found for $request.method:$request.requestURI $context"
				chain.doFilter(request, response)
			}
		} finally {
			destroyContext()
		}
	}

	/**
	 * Overrides buildPageInfo in PageFragmentCachingFilter to use different cache depending on target controller rather
	 * than having the cache wired into the filter.
	 */
	@Override protected PageInfo buildPageInfo(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		def timer = new Timer()
		timer.start(getCachedUri(request))
		boolean isCached = true

		def cache = springcacheService.getOrCreateBlockingCache(context.cacheName)

		def key = calculateKey(request)

		def pageInfo
		try {
			def element = cache.get(key)
			if (!element || element.isExpired() || !element.objectValue) {
				isCached = false
				try {
					// Page is not cached - build the response, cache it, and send to client
					pageInfo = buildPage(request, response, chain)
					if (pageInfo.isOk()) {
						log.debug "PageInfo ok. Adding to cache $cache.name with key $key"
						cache.put(new Element(key, pageInfo))
					} else {
						log.debug "PageInfo was not ok(200). Putting null into cache $cache.name with key $key"
						cache.put(new Element(key, null))
					}
				} catch (Throwable t) {
					// Must unlock the cache if the above fails. Will be logged at Filter
					cache.put(new Element(key, null))
					throw t
				}
			} else {
				log.debug "Serving cached content for $key"
				pageInfo = element.objectValue

				// As the page is cached, we need to add an instance of the associated
				// controller to the request. This is required by GrailsLayoutDecoratorMapper
				// to pick the appropriate layout.
				if (context?.controllerName) {
					def controller = context.controllerArtefact.newInstance()
					request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)
				}
			}
		} catch (LockTimeoutException e) {
			//do not release the lock, because you never acquired it
			throw e
		}

		timer.stop(isCached)
		return pageInfo
	}

	/**
	 * Overrides buildPage in PageFragmentCachingFilter to use different cache depending on target controller and to do
	 * special handling for Grails include requests.
	 */
	@Override protected PageInfo buildPage(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
		// Invoke the next entity in the chain
		def outstr = new ByteArrayOutputStream()
		def wrapper = new GenericResponseWrapper(response, outstr)

		// TODO: split the special include handling out into a separate method
		def originalResponse = null
		def isInclude = WebUtils.isIncludeRequest(request)
		if (isInclude) {
			originalResponse = WrappedResponseHolder.wrappedResponse
			WrappedResponseHolder.wrappedResponse = wrapper
		}
		try {
			chain.doFilter(request, wrapper)
		} finally {
			if (isInclude) {
				WrappedResponseHolder.wrappedResponse = originalResponse
			}
		}
		wrapper.flush()

		long timeToLiveSeconds = cacheManager.getEhcache(context.cacheName).cacheConfiguration.timeToLiveSeconds

		def contentType = wrapper.contentType ?: response.contentType
		return new PageInfo(wrapper.status, contentType, wrapper.headers, wrapper.cookies,
				outstr.toByteArray(), false, timeToLiveSeconds)
	}

	/**
	 * Overrides writeResponse in PageFragmentCachingFilter to set the contentType before writing the response. This is
	 * necessary so that Sitemesh is activated (yeah, setContentType on GrailsContentBufferingResponse has a
	 * side-effect) and will decorate our cached response.
	 */
	@Override protected void writeResponse(HttpServletResponse response, PageInfo pageInfo) {
		response.contentType = pageInfo.contentType
		super.writeResponse(response, pageInfo)
	}

	@Override protected CacheManager getCacheManager() {
		throw new UnsupportedOperationException("Not supported in this implementation")
	}

	@Override protected String calculateKey(HttpServletRequest request) {
		def keyGenerator = context.keyGenerator ?: defaultKeyGenerator
		return keyGenerator.generateKey(context).toString()
	}

	boolean handleFlush(HttpServletRequest request) {
		CacheFlush cacheFlush = getAnnotation(context, CacheFlush)
		if (cacheFlush) {
			logRequestDetails(request, context, "Flushing request")
			springcacheService.flush(cacheFlush.value())
			return true
		} else {
			log.debug "No cacheflush annotation found for $request.method:$request.requestURI $context"
			return false
		}
	}

	private Annotation getAnnotation(FilterContext context, Class type) {
		// TODO: cache this by controller/action
		def annotation = context.actionClosure?.getAnnotation(type)
		if (!annotation) {
			annotation = context.controllerArtefact?.clazz?.getAnnotation(type)
		}
		return annotation
	}

	private void logRequestDetails(HttpServletRequest request, FilterContext context, String message) {
		if (log.isDebugEnabled()) {
			log.debug "$message..."
			log.debug "    method = $request.method"
			log.debug "    requestURI = $request.requestURI"
			log.debug "    forwardURI = $request.forwardURI"
			if (WebUtils.isIncludeRequest(request)) {
				log.debug "    includeURI = ${request[WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE]}"
			}
			log.debug "    controller = $context.controllerName"
			log.debug "    action = $context.actionName"
			log.debug "    format = $request.format"
			log.debug "    params = $context.params"
		}
	}

	private String getCachedUri(HttpServletRequest request) {
		if (WebUtils.isIncludeRequest(request)) {
			return request[WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE]
		}
		return request.requestURI
	}

	private void initContext() {
		contextHolder.set(new FilterContext())
	}

	private FilterContext getContext() {
		contextHolder.get()
	}

	private void destroyContext() {
		contextHolder.remove()
	}

}

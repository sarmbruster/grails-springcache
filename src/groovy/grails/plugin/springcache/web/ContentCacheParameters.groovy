package grails.plugin.springcache.web

import grails.plugin.springcache.CacheParameters
import javax.servlet.http.HttpServletRequest

class ContentCacheParameters implements CacheParameters {

	final String controllerName
	final String actionName
	final Map params
	final HttpServletRequest request

	ContentCacheParameters(requestAttributes) {
		controllerName = requestAttributes.controllerName
		actionName = requestAttributes.actionName
		params = requestAttributes.params
		request = requestAttributes.request
	}

}

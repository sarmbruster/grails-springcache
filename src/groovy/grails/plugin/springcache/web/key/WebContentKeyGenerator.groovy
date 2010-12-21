package grails.plugin.springcache.web.key

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.ContentCacheParameters

class WebContentKeyGenerator extends DefaultKeyGenerator {

	static final String X_REQUESTED_WITH = "X-Requested-With"

	boolean ajax
	boolean contentType
	boolean requestMethod

	@Override protected void generateKeyInternal(CacheKeyBuilder builder, ContentCacheParameters context) {
		super.generateKeyInternal(builder, context)

		if (ajax) handleAjax(builder, context)
		if (contentType) handleContentType(builder, context)
		if (requestMethod) handleRequestMethod(builder, context)
	}

	void handleAjax(CacheKeyBuilder builder, ContentCacheParameters context) {
		builder << "$X_REQUESTED_WITH: ${context.request.getHeader(X_REQUESTED_WITH)}"
	}

	void handleContentType(CacheKeyBuilder builder, ContentCacheParameters context) {
		def contentFormat = context.request.format
		if (contentFormat && contentFormat != "all") {
			builder << "format: " << contentFormat
		}
	}

	void handleRequestMethod(CacheKeyBuilder builder, ContentCacheParameters context) {
		def method = context.request.method
		if (method in ["GET", "HEAD"]) method = "GET/HEAD"
		builder << "method: $method"
	}

}

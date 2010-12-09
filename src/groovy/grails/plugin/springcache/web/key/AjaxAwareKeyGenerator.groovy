package grails.plugin.springcache.web.key

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.ContentCacheParameters

class AjaxAwareKeyGenerator extends DefaultKeyGenerator {

	static final String X_REQUESTED_WITH = "X-Requested-With"

	@Override protected void generateKeyInternal(CacheKeyBuilder builder, ContentCacheParameters context) {
		super.generateKeyInternal builder, context
		if (context.request.xhr) {
			builder << ":AJAX"
		}
	}


}

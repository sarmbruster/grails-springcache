/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.web.key

import grails.plugin.springcache.key.CacheKeyBuilder
import grails.plugin.springcache.web.ContentCacheParameters

class WebContentKeyGenerator extends DefaultKeyGenerator {

	static final String X_REQUESTED_WITH = "X-Requested-With" // missing from org.codehaus.groovy.grails.web.servlet.HttpHeaders

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

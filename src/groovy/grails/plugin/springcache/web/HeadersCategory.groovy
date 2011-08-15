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
package grails.plugin.springcache.web

import javax.servlet.http.HttpServletRequest
import net.sf.ehcache.constructs.web.PageInfo
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*

@Category(PageInfo)
class HeadersCategory {

	String getHeader(String headerName) {
		headers.find { it.name == headerName }?.value
	}

	long getDateHeader(String headerName) {
		def header = getHeader(headerName)
		if (header?.isLong()) {
			header.toLong()
		} else if (header) {
			httpDateFormatter.parseDateFromHttpDate(header).time
		} else {
			-1
		}
	}

	/**
	 * Returns true if the page's last-modified header indicates it is newer than the copy held by the client as
	 * indicated by the request's if-modified-since header.
	 */
	boolean isModified(HttpServletRequest request) {
		long ifModifiedSince = request.getDateHeader(IF_MODIFIED_SINCE)
		long lastModified = getDateHeader(LAST_MODIFIED)
		if (ifModifiedSince == -1 || lastModified == -1) true
		else lastModified > ifModifiedSince
	}

	/**
	 * Returns true if the page's etag header indicates it is the same as the copy held by the client as
	 * indicated by the request's if-none-match header.
	 */
	boolean isMatch(HttpServletRequest request) {
		def ifNoneMatch = request.getHeader(IF_NONE_MATCH)
		def etag = getHeader(ETAG)
		if (!ifNoneMatch || !etag) false
		else ifNoneMatch == etag
	}

	Map<String, ?> getCacheDirectives() {
		def cacheControl = getHeader(CACHE_CONTROL)
		def directives = [:]
		if (cacheControl) {
			cacheControl.split(/,\s*/).each { String directive ->
				directive.find(/([\w-]+)(?:=(.+))?/) { match, name, value ->
					if (!value) {
						directives[name] = true
					} else if (value.isNumber()) {
						directives[name] = value.toInteger()
					} else {
						directives[name] = value
					}
				}
			}
		}
		directives
	}

}

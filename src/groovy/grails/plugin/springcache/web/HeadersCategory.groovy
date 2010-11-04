package grails.plugin.springcache.web

import net.sf.ehcache.constructs.web.PageInfo
import javax.servlet.http.HttpServletRequest
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*

@Category(PageInfo)
class HeadersCategory {

	String getHeader(String headerName) {
		headers.find { it.name == headerName }?.value
	}

	long getDateHeader(String headerName) {
		def header = getHeader(headerName)
		if (header) {
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

}

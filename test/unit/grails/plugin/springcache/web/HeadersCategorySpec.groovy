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

import grails.plugin.spock.UnitSpec
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Unroll
import static javax.servlet.http.HttpServletResponse.SC_OK
import net.sf.ehcache.constructs.web.*
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*

class HeadersCategorySpec extends UnitSpec {

	HttpDateFormatter formatter = new HttpDateFormatter()

	def setup() {
		registerMetaClass PageInfo
		PageInfo.metaClass.mixin(HeadersCategory)
	}

	@Unroll
	def "can use headers to identify newer content"() {
		given:
		def headers = []
		if (lastModified) headers << ([LAST_MODIFIED, lastModified] as String[])
		def pageInfo = new PageInfo(SC_OK, "text/html", headers, [], new byte[0], false, 0L)

		and:
		def request = new MockHttpServletRequest()
		if (ifModifiedSince) request.addHeader(IF_MODIFIED_SINCE, formatter.parseDateFromHttpDate(ifModifiedSince))

		expect:
		pageInfo.isModified(request) == isModified

		where:
		lastModified                    | ifModifiedSince                 | isModified
		"Wed, 03 Nov 2010 21:11:22 GMT" | "Wed, 03 Nov 2010 21:11:22 GMT" | false
		"Wed, 02 Nov 2010 21:11:22 GMT" | "Wed, 01 Nov 2010 21:11:22 GMT" | true
		"Wed, 01 Nov 2010 21:11:22 GMT" | "Wed, 02 Nov 2010 21:11:22 GMT" | false
		null                            | "Wed, 03 Nov 2010 21:11:22 GMT" | true
		"Wed, 03 Nov 2010 21:11:22 GMT" | null                            | true
	}

	@Unroll
	def "can use headers to identify matching content"() {
		given:
		def headers = []
		if (eTag) headers << ([ETAG, eTag] as String[])
		def pageInfo = new PageInfo(SC_OK, "text/html", headers, [], new byte[0], false, 0L)

		and:
		def request = new MockHttpServletRequest()
		if (ifNoneMatch) request.addHeader(IF_NONE_MATCH, ifNoneMatch)

		expect:
		pageInfo.isMatch(request) == isMatch

		where:
		eTag | ifNoneMatch | isMatch
		"B"  | "A"         | false
		"A"  | "A"         | true
		"A"  | null        | false
		null | "A"         | false
		null | null        | false
	}

	@Unroll
	def "can decode cache control directives"() {
		given:
		def headers = []
		if (cacheControl) headers << ([CACHE_CONTROL, cacheControl] as String[])
		def pageInfo = new PageInfo(SC_OK, "text/html", headers, [], new byte[0], false, 0L)

		expect:
		pageInfo.cacheDirectives == directives

		where:
		cacheControl                                                     | directives
		null                                                             | [:]
		"no-cache"                                                       | ["no-cache": true]
		"max-age=21600"                                                  | ["max-age": 21600]
		"no-cache, no-store, must-revalidate, pre-check=0, post-check=0" | ["no-cache": true, "no-store": true, "must-revalidate": true, "pre-check": 0, "post-check": 0]
	}

}

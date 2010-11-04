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

}

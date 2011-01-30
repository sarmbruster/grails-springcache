package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import groovyx.net.http.RESTClient
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static javax.servlet.http.HttpServletResponse.SC_OK
import spock.lang.*

@Issue("http://jira.codehaus.org/browse/GRAILSPLUGINS-2769")
@Stepwise
class FragmentCachingSpec extends AbstractContentCachingSpec {
	
	@Shared Ehcache popularControllerCache = ApplicationHolder.application.mainContext.popularControllerCache

	def "uncached fragments are served correctly"() {
		when: "the popular album module is requested"
		go "http://localhost:8080/popular/albums"
		
		then: "the response is rendered correctly"
		$("h2")
		
		and: "the cache was missed"
		popularControllerCache.statistics.cacheHits == 0L
		popularControllerCache.statistics.cacheMisses == old(popularControllerCache.statistics.cacheMisses) + 1
	}

	def "cached fragments are served correctly"() {
		when: "the popular album module is requested again"
		go "http://localhost:8080/popular/albums"
		
		then: "the response is not empty"
		$("h2")
		
		and: "the cache was hit"
		popularControllerCache.statistics.cacheHits == old(popularControllerCache.statistics.cacheHits) + 1
	}

}
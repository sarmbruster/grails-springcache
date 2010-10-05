package grails.plugin.springcache.web

import grails.plugin.geb.GebSpec
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.Shared

/**
 * Functional tests to ensure that layouts are rendered correctly when
 * a view is cached, regardless of whether the layout is specified explicitly
 * or applied by convention.
 */
class LayoutsSpec extends AbstractContentCachingSpec {

	@Shared simpleCache = ApplicationHolder.application.mainContext.simpleCache
	
	def "Layout by controller name renders correctly with cached view"() {
		given: "The view is hit once and cached."
		go "/simple/index"

		when: "I load the page again"
		go "/simple/index"

		then: "The view and layout are rendered correctly"
		page.title == "Simple stuff"
		$("body").text() == "Index page"

		and: "The cache is used"
		simpleCache.statistics.cacheHits == 1L
	}
	
	def "Layout by action name renders correctly with cached view"() {
		given: "The view is hit once and cached."
		go "/simple/hello"

		when: "I load the page again"
		go "/simple/hello"

		then: "The view and layout are rendered correctly"
		page.title == "Hello world!"
		$("body").text() == "Hello world!"

		and: "The cache is used"
		simpleCache.statistics.cacheHits == 1L
	}
}

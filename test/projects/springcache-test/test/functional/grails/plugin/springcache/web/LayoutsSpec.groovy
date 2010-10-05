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

	@Shared layoutsCache = ApplicationHolder.application.mainContext.layoutsCache
	
	def "Layout by controller name renders correctly with cached view"() {
		given: "The view is hit once and cached."
		go "/simple/index"

		when: "I load the page again"
		go "/simple/index"

		then: "The view and layout are rendered correctly"
		page.title == "Simple stuff"
		$("body").text() == "Index page"

		and: "The cache is used"
		layoutsCache.statistics.cacheHits == 1L
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
		layoutsCache.statistics.cacheHits == 1L
	}

	def "Layout by static property renders correctly with cached view"() {
		given: "The view is hit once and cached"
		go "/staticLayout/index"

		when: "I load the page again"
		go "/staticLayout/index"

		then: "the view and layout are rendered correctly"
		page.title == "Avast there!"
		$("body").text() == "Yarr!"

		and: "The cache is hit"
		layoutsCache.statistics.cacheHits == 1L
	}

	def "Layout by config renders correctly with cached view"() {
		given: "The view is hit once and cached"
		go "/noLayout/index"

		when: "I load the page again"
		go "/noLayout/index"

		then: "the view and layout are rendered correctly"
		page.title == "Configured default layout!"
		$("body").text() == "O HAI!"

		and: "The cache is hit"
		layoutsCache.statistics.cacheHits == 1L
	}
}

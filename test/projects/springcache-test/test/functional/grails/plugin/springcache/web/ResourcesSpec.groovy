package grails.plugin.springcache.web

import spock.lang.Stepwise

@Stepwise
class ResourcesSpec extends AbstractContentCachingSpec {

	def "resources are loaded when the page is not cached"() {
		given:
		go "/resources"

		expect:
		$("#added-by-jquery").size() == 1
	}

	def "resources are loaded when the page is cached"() {
		given:
		go "/resources"

		expect:
		$("#added-by-jquery").size() == 1
	}

}

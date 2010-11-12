package grails.plugin.springcache.taglib

import grails.plugin.geb.GebSpec
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.*

@Stepwise
class TagLibCachingSpec extends GebSpec {

	@Shared def grailsApplication = ApplicationHolder.application
	@Shared def springcacheService = grailsApplication.mainContext.springcacheService
	
	def setup() {
		clearCache()
		tagLibValue = 1
	}
	
	def "using tag across different controller and actions"() {
		expect: "initial request should invoke tag"
		a1 == "1 2"

		and: "subsequent hits cache"
		a1 == "1 3"

		and:"request to different action hits the cache"
		a2 == "1 4"
		
		and: "request to different controller using same tag hits the cache"
		b1 == "1 5"
		
		and: "request to different controller and action using same tag hits the cache"
		b2 == "1 6"
		
		when: "the cache is cleared"
		clearCache()
		
		then: "a new value is used"
		a1 == "7 8"
	}
	
	def "exercise using params on caching tags"() {
		expect: "initial request gets initial value"
		withParams(a: 1, b: 2) == "1"
		
		and: "same params gets cached value"
		withParams(a: 1, b: 2) == "1"

		and: "same params in a different order gets cached value"
		withParams(a: 1, b: 2, flip: true) == "1"
		
		and: "different params gets new value"
		withParams(a: 2, b: 1) == "2"
		
		and: "initial params gets initial value"
		withParams(a: 1, b: 2) == "1"
		
		and: "changing just one param gets new value"
		withParams(a: 2, b: 2) == "3"
	}
	
	def "using a non cached tag that renders the value of a cached tag"() {
		expect:
		indirect == "1"

		and: "subsequent hits cache"
		indirect == "1"
		
		when:
		clearCache()
		
		then:
		indirect == "2"
	}
		
	def "using a caching tag with a body"() {
		expect:
		withBody("1") == "1"

		and: "subsequent hits cache"
		withBody("2") == "1"
		
		when:
		clearCache("testCachingTagLibWithBody")
		
		then:
		withBody("5") == "5"
	}
	
	
	protected clearCache(name = "tagLibCache") {
		springcacheService.flush(name)
	}
	
	protected void setTagLibValue(value) {
		grailsApplication.mainContext['taglib.TestCachingTagLib'].setValue(value)
	}
	
	protected String getA1() {
		getText("tagUsingA", "a1")
	}
	
	protected String getA2() {
		getText("tagUsingA", "a2")
	}

	protected String getB1() {
		getText("tagUsingB", "a1")
	}

	protected String getB2() {
		getText("tagUsingB", "a2")
	}

	protected String withParams(params) {
		getText("tagUsingA", "withParams", params)
	}
	
	protected String getIndirect() {
		getText("tagUsingA", "indirect")
	}

	protected String withBody(body) {
		getText("tagUsingA", "withBody", [body: body])
	}
	
	protected String getText(controllerName, actionName, params = null) {
		def url = "/$controllerName/$actionName"
		if (params) {
			// we aren't encoding here, don't use things requiring encoding
			url += "?" + params.collect { k,v -> "$k=$v" }.join("&")
		}
		go url
		find("body").text()
	}
	

}
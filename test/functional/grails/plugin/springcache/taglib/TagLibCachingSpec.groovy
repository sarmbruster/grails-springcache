package grails.plugin.springcache.taglib

import spock.lang.*
import grails.plugin.spock.*
import grails.util.BuildSettingsHolder
import grails.plugin.remotecontrol.RemoteControl

@Stepwise
class TagLibCachingSpec extends IntegrationSpec {

	@Shared remote = new RemoteControl()
	
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
	
	
	protected clearCache(name = "testCachingTagLib") {
		remote.exec {
			springcacheService.flush(name)
		}
	}
	
	protected void setTagLibValue(value) {
		remote.exec {
			grailsApplication.mainContext['grails.plugin.springcache.test.TestCachingTagLib'].setValue(value)
		}
		
		// issue with RC plugin - for some reason setting a property carries a reference to 'this' class
		// results in a NoClassDefFoundError for TagLibCachingSpec, I don't know why
		//grailsApplication.mainContext['grails.plugin.springcache.test.TestCachingTagLib'].value = value
	}
	
	protected getA1() {
		getText("tagUsingA", "a1")
	}
	
	protected getA2() {
		getText("tagUsingA", "a2")
	}

	protected getB1() {
		getText("tagUsingB", "a1")
	}

	protected getB2() {
		getText("tagUsingB", "a2")
	}

	protected withParams(params) {
		getText("tagUsingA", "withParams", params)
	}
	
	protected getIndirect() {
		getText("tagUsingA", "indirect")
	}

	protected withBody(body) {
		getText("tagUsingA", "withBody", [body: body])
	}
	
	protected getText(controllerName, actionName, params = null) {
		getUrl(controllerName, actionName, params).text
	}
	
	protected getUrl(controllerName, actionName, params = null) {
		def url = baseUrl + controllerName + "/" + actionName
		if (params) {
			// we aren't encoding here, don't use things requiring encoding
			url += "?" + params.collect { k,v -> "$k=$v" }.join("&")
		}
		new URL(url)
	}
	
	protected getBaseUrl() {
		def base = BuildSettingsHolder.settings.functionalTestBaseUrl
		base.endsWith("/") ? base : base + "/"
	}

}
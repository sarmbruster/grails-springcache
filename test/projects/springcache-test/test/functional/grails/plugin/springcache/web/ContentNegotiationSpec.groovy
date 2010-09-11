package grails.plugin.springcache.web

import spock.lang.*
import grails.plugin.springcache.SpringcacheService
import musicstore.*
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import groovyx.net.http.*

class ContentNegotiationSpec extends Specification {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared Ehcache latestControllerCache = ApplicationHolder.application.mainContext.latestControllerCache
	
	def setupSpec() {
		Album.withNewSession {
			def artist = Artist.build(name: "The Cure")
			Album.build(artist: artist, name: "Pornography", year: "1982")
		}
	}

	def cleanupSpec() {
		Album.withNewSession {
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	@Unroll("content requested with content type '#contentType' is cached separately")
	def "content requested with different formats is cached separately"() {
		when: "the latest album module is requested in a particular format"
		def response = new RESTClient().get(uri: "http://localhost:8080/latest/albums", headers : [Accept : contentType])
		
		then: "the correct content type is returned"
		response.status == 200
		response.contentType == contentType
		
		and: "the response is cached separately"
		latestControllerCache.statistics.cacheHits == 0L
		latestControllerCache.statistics.cacheMisses == old(latestControllerCache.statistics.cacheMisses) + 1
		latestControllerCache.statistics.objectCount == old(latestControllerCache.statistics.objectCount) + 1
		
		where:
		contentType << ["text/html", "text/xml", "application/json"]
	}

}
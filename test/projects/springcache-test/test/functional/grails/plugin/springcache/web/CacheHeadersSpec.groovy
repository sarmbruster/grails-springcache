package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import groovyx.net.http.RESTClient
import net.sf.ehcache.Ehcache
import org.apache.http.protocol.HttpDateGenerator
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static javax.servlet.http.HttpServletResponse.*
import musicstore.*
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*
import spock.lang.*

class CacheHeadersSpec extends Specification {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache

	@Shared private Album album
	private RESTClient http = new RESTClient()
	@Shared private HttpDateGenerator date = new HttpDateGenerator()

	def setupSpec() {
		album = Album.withNewSession {
			Album.build(artist: new Artist(name: "Les Savy Fav"), name: "Root For Ruin", year: "2010")
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

	def "cache control headers from the original response are served with a cached response"() {
		given: "the cache is primed by an previous request"
		def response1 = http.get(uri: "http://localhost:8080/album/show/$album.id")

		when: "the same action is invoked again"
		def response2 = http.get(uri: "http://localhost:8080/album/show/$album.id")

		then: "the content is served from the cache"
		response2.status == SC_OK
		albumControllerCache.statistics.cacheHits == 1L

		and: "cache control headers are served with the cached response"
		response1.headers[LAST_MODIFIED].value == response2.headers[LAST_MODIFIED].value
		response1.headers[ETAG].value == response2.headers[ETAG].value
	}

	@Unroll("a 304 is served rather than a cached response if the client sends #headers")
	def "a 304 is served rather than a cached response if the client has cached the response"() {
		given: "the cache is primed by an previous request"
		http.get(uri: "http://localhost:8080/album/show/$album.id")

		when: "the same action is invoked again with an if-modified-since header"
		def response = http.get(uri: "http://localhost:8080/album/show/$album.id", headers: headers)

		then: "the server responds with a 304"
		response.status == SC_NOT_MODIFIED

		and: "no content is served"
		response.data == null

		where:
		headers << [[(IF_MODIFIED_SINCE): date.currentDate], [(IF_NONE_MATCH): "$album.id:$album.version"]]
	}

}

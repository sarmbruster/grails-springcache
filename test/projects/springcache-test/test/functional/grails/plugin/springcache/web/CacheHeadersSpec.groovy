package grails.plugin.springcache.web

import grails.plugin.springcache.SpringcacheService
import groovyx.net.http.RESTClient
import musicstore.Album
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import static javax.servlet.http.HttpServletResponse.SC_OK
import spock.lang.*
import musicstore.Artist
import static org.codehaus.groovy.grails.web.servlet.HttpHeaders.*

class CacheHeadersSpec extends Specification {

	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache

	def cleanupSpec() {
		Album.withNewSession {
			Album.list()*.delete()
			Artist.list()*.delete()
		}

		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	def "cache control headers from the original response are served with a cached response"() {
		given: "an album"
		def album = Album.withNewSession {
			Album.build(artist: new Artist(name: "Les Savy Fav"), name: "Root For Ruin", year: "2010")
		}

		and: "the cache is primed by an previous request"
		def response1 = new RESTClient().get(uri: "http://localhost:8080/album/show/$album.id")

		when: "the same action is invoked again"
		def response2 = new RESTClient().get(uri: "http://localhost:8080/album/show/$album.id")

		then: "the content is served from the cache"
		response2.status == SC_OK
		albumControllerCache.statistics.cacheHits == 1L

		and: "cache control headers are served with the cached response"
		response1.headers[LAST_MODIFIED].value == response2.headers[LAST_MODIFIED].value
	}

}

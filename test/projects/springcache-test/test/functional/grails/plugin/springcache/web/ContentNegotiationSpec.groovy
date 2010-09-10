package grails.plugin.springcache.web

import functionaltestplugin.FunctionalTestCase
import grails.plugin.springcache.SpringcacheService
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import static grails.plugin.springcache.matchers.CacheHitsMatcher.hasCacheHits
import static grails.plugin.springcache.matchers.CacheMissesMatcher.hasCacheMisses
import static grails.plugin.springcache.matchers.CacheSizeMatcher.hasCacheSize
import static javax.servlet.http.HttpServletResponse.SC_OK
import static org.hamcrest.MatcherAssert.assertThat
import grails.plugin.geb.GebSpec

class ContentNegotiationSpec extends GebSpec {

	SpringcacheService springcacheService
	Ehcache latestControllerCache

	def setup() {
		def port = System.properties."server.port" ?: "8080"
		baseURL = "http://localhost:$port"

		javaScriptEnabled = false

		Album.withTransaction { tx ->
			def artist = Artist.build(name: "The Cure")
			Album.build(artist: artist, name: "Pornography", year: "1982")
		}
	}

	void tearDown() {
		super.tearDown()

		Album.withTransaction {tx ->
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	void testCachedContentNotServedWhenAcceptHeaderIsDifferent() {
		get "/latest/albums"
		assertStatus SC_OK
		assertContentType "text/html"
		assertThat latestControllerCache, hasCacheHits(0)
		assertThat latestControllerCache, hasCacheMisses(1)
		assertThat latestControllerCache, hasCacheSize(1)

		get("/latest/albums") {
			headers["Accept"] = "text/xml"
		}
		assertStatus SC_OK
		assertContentType "text/xml"
		assertThat latestControllerCache, hasCacheHits(0)
		assertThat latestControllerCache, hasCacheMisses(2)
		assertThat latestControllerCache, hasCacheSize(2)
	}

}
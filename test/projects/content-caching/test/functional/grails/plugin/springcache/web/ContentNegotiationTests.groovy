package grails.plugin.springcache.web

import functionaltestplugin.FunctionalTestCase
import grails.plugin.springcache.SpringcacheService
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import static javax.servlet.http.HttpServletResponse.SC_OK
import org.junit.Before
import org.junit.After
import org.junit.Test
import static org.junit.Assert.*
import static org.hamcrest.CoreMatchers.*

class ContentNegotiationTests extends FunctionalTestCase {

	SpringcacheService springcacheService
	Ehcache latestControllerCache

	@Before
	void setUp() {
		super.setUp()

		def port = System.properties."server.port" ?: "8080"
		baseURL = "http://localhost:$port"

		javaScriptEnabled = false

		Album.withTransaction { tx ->
			def artist = Artist.build(name: "The Cure")
			Album.build(artist: artist, name: "Pornography", year: "1982")
		}
	}

	@After
	void tearDown() {
		super.tearDown()

		Album.withTransaction {tx ->
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}

	@Test
	void cachedContentNotServedWhenAcceptHeaderIsDifferent() {
		get "/"
		assertStatus SC_OK
		assertContentType "text/html"
		assertThat "cache hits", latestControllerCache.statistics.cacheHits, equalTo(0L)
		assertThat "cache misses", latestControllerCache.statistics.cacheMisses, equalTo(1L)
		assertThat "cache size", latestControllerCache.statistics.objectCount, equalTo(1L)

		get("/latest/albums") {
			headers["Accept"] = "text/xml"
		}
		assertStatus SC_OK
		assertContentType "text/xml"
		assertThat "cache hits", latestControllerCache.statistics.cacheHits, equalTo(0L)
		assertThat "cache misses", latestControllerCache.statistics.cacheMisses, equalTo(2L)
		assertThat "cache size", latestControllerCache.statistics.objectCount, equalTo(2L)
	}

}
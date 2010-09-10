package grails.plugin.springcache.web

import spock.lang.*
import musicstore.*
import musicstore.pages.*
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder

class StaticScaffoldingCachingSpec extends AbstractContentCachingSpec {

	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache

	def cleanup() {
		Album.withTransaction {
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	def "opening a page when the cache is empty misses the cache"() {
		when: "I open the album list page"
		to AlbumListPage

		then: "the cache is missed"
		albumControllerCache.statistics.cacheHits == 0L
		albumControllerCache.statistics.cacheMisses == 1L
	}

	def "reloading a page hits the cache"() {
		when: "I open and refresh the album list page"
		to AlbumListPage
		driver.navigate().refresh()

		then: "the cache is missed once and hit once"
		albumControllerCache.statistics.cacheHits == 1L
		albumControllerCache.statistics.cacheMisses == 1L
	}

	def "cached content can be flushed by other actions"() {
		given: "the album list cache is primed"
		to AlbumListPage

		when: "I create a new album"
		to AlbumCreatePage
		albumForm.artist = "Edward Sharpe & the Magnetic Zeros"
		albumForm.name = "Up From Below"
		albumForm.year = "2009"
		createButton.click()
		to AlbumListPage

		then: "the cached content is not displayed"
		rows.size() == old(rows.size()) + 1

		and: "the cache is flushed"
		albumControllerCache.statistics.cacheHits == 0L
		albumControllerCache.statistics.cacheMisses == 3L // 2 misses on list page, 1 on show
		albumControllerCache.statistics.objectCount == 2L // show and list pages cached
	}

	def "failed save still flushes cache"() {
		given: "the album list cache is primed"
		to AlbumListPage

		when: "I attempt to save a new album but with invalid data"
		to AlbumCreatePage
		createButton.click(AlbumCreatePage)

		then: "the album should not be saved"
		errorMessages.contains("Property [artist] of class [class musicstore.Album] cannot be null")
		Album.count() == old(Album.count())

		when: "I open the list page again"
		to AlbumListPage

		then: "it should miss the cache as the save action flushed it"
		albumControllerCache.statistics.cacheHits == 0L
		albumControllerCache.statistics.cacheMisses == 2L
		albumControllerCache.statistics.objectCount == 1L
	}

	def "different show pages are cached separately"() {
		given:
		def albumIds = []
		Album.withNewSession {
			def artist = Artist.build(name: "Metric")
			albumIds << Album.build(artist: artist, name: "Fantasies", year: "2009").id
			albumIds << Album.build(artist: artist, name: "Live It Out", year: "2005").id
		}

		when: "I visit two different show album pages"
		to AlbumShowPage, albumIds[0]
		to AlbumShowPage, albumIds[1]

		then: "the cache is missed both times"
		albumControllerCache.statistics.cacheHits == 0L
		albumControllerCache.statistics.cacheMisses == 2L
		albumControllerCache.statistics.objectCount == 2L
	}

	def "an invalid show page does not get cached"() {
		when: "I try to visit an album show page that doesn't exist"
		to AlbumShowPage, 404
		page AlbumListPage
		
		then: "I see the not found page"
		flashMessage == "Album not found with id 404"

		and: "the cache was not hit"
		albumControllerCache.statistics.objectCount == 1L // the cache will have been primed by the list page
	}
}

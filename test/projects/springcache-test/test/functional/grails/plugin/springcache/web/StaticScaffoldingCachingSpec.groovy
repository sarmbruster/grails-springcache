package grails.plugin.springcache.web

import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.Shared
import musicstore.*
import musicstore.pages.*

class StaticScaffoldingCachingSpec extends AbstractContentCachingSpec {

	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache

	def cleanup() {
		Album.withNewSession {
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	def "opening a page when the cache is empty misses the cache"() {
		when: "I open the album list page"
		to AlbumListPage

		then: "the cache is missed"
		cacheHits == 0L
		cacheMisses == old(cacheMisses) + 1
	}

	def "reloading a page hits the cache"() {
		given: "I am on the album list page"
		to AlbumListPage
		
		when: "I refresh the page"
		driver.navigate().refresh()

		then: "the cache is missed once and hit once"
		cacheHits == 1L
		cacheMisses == 1L
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

		and: "return to the list page"
		to AlbumListPage

		then: "the cached content is not displayed"
		rows.size() == old(rows.size()) + 1

		and: "the cache is flushed"
		cacheHits == 0L
		cacheMisses == 4L // 2 misses on list page, 1 on create & 1 on show
		cacheSize == 2L // show and list pages cached
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
		cacheHits == 0L
		cacheMisses == 2L
		cacheSize == 1L
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
		cacheHits == 0L
		cacheMisses == 2L
		cacheSize == 2L
	}

	def "an invalid show page does not get cached"() {
		when: "I try to visit an album show page that doesn't exist"
		to AlbumShowPage, 404
		page AlbumListPage
		
		then: "I see the not found page"
		flashMessage == "Album not found with id 404"

		and: "the cache was not hit"
		cacheSize == 1L // the cache will have been primed by the list page
	}

	private long getCacheHits() {
		return albumControllerCache.statistics.cacheHits
	}

	private long getCacheMisses() {
		return albumControllerCache.statistics.cacheMisses
	}

	private long getCacheSize() {
		return albumControllerCache.statistics.objectCount
	}

}

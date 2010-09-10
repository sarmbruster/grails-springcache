package grails.plugin.springcache.web

import musicstore.Album
import musicstore.Artist
import musicstore.pages.AlbumCreatePage
import musicstore.pages.AlbumListPage
import musicstore.pages.AlbumShowPage
import net.sf.ehcache.Ehcache
import static grails.plugin.springcache.matchers.CacheHitsMatcher.hasCacheHits
import static grails.plugin.springcache.matchers.CacheMissesMatcher.hasCacheMisses
import static grails.plugin.springcache.matchers.CacheSizeMatcher.hasCacheSize
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class StaticScaffoldingCachingTests extends AbstractContentCachingSpec {

	Ehcache albumControllerCache

	void tearDown() {
		Album.withTransaction {
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		super.tearDown()
	}

	void testOpeningListPageWithEmptyCache() {
		AlbumListPage.open()

		assertThat albumControllerCache, hasCacheHits(0)
		assertThat albumControllerCache, hasCacheMisses(1)
	}

	void testReloadingListPageHitsCache() {
		def page = AlbumListPage.open()

		page.refresh()

		assertThat albumControllerCache, hasCacheHits(1)
		assertThat albumControllerCache, hasCacheMisses(1)
	}

	void testSaveFlushesCache() {
		def listPage = AlbumListPage.open()
		assertThat "Album list size", listPage.rowCount, equalTo(0)

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Edward Sharpe & the Magnetic Zeros"
		createPage.name = "Up From Below"
		createPage.year = "2009"
		createPage.save()

		assertThat "Album count", Album.count(), equalTo(1)

		listPage = AlbumListPage.open()
		assertThat "Album list page is still displayed cached content", listPage.rowCount, equalTo(1)

		assertThat albumControllerCache, hasCacheHits(0)
		assertThat albumControllerCache, hasCacheMisses(3) // 2 misses on list page, 1 on show
		assertThat albumControllerCache, hasCacheSize(2) // show and list pages cached
	}

	void testFailedSaveStillFlushesCache() {
		// opening the list page should miss the cache
		AlbumListPage.open()

		// attempt to save a new album but with invalid data
		def createPage = AlbumCreatePage.open()
		createPage.artist = ""
		createPage.name = "Up From Below"
		createPage.year = "2009"
		createPage.saveExpectingFailure()

		// the album should not have been saved
		assertThat "Error message", createPage.errorMessages, hasItem("Property [artist] of class [class musicstore.Album] cannot be null")
		assertThat "Album count", Album.count(), equalTo(0)

		// however, if we open the list page again it should miss the cache as the save action flushed
		AlbumListPage.open()

		assertThat albumControllerCache, hasCacheHits(0)
		assertThat albumControllerCache, hasCacheMisses(2)
		assertThat albumControllerCache, hasCacheSize(1)
	}

	void testDifferentShowPagesCachedSeparately() {
		def artist = Artist.build(name: "Metric")
		def album1 = Album.build(artist: artist, name: "Fantasies", year: "2009")
		def album2 = Album.build(artist: artist, name: "Live It Out", year: "2005")

		def showPage1 = AlbumShowPage.open(album1.id)
		assertThat "Album name", showPage1.Name, equalTo(album1.name)

		def showPage2 = AlbumShowPage.open(album2.id)
		assertThat "Album name", showPage2.Name, equalTo(album2.name)

		assertThat albumControllerCache, hasCacheHits(0)
		assertThat albumControllerCache, hasCacheMisses(2)
		assertThat albumControllerCache, hasCacheSize(2)
	}

	void testNotFoundDoesNotGetCached() {
		def page = AlbumShowPage.openInvalidId(404)
		assertThat "Flash message", page.flashMessage, equalTo("Album not found with id 404")

		// cache size will be 1 as list page is returned
		assertThat albumControllerCache, hasCacheSize(1)
	}
}

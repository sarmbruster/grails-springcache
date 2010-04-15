package grails.plugin.springcache.web

import musicstore.Album
import musicstore.Artist
import musicstore.pages.AlbumCreatePage
import musicstore.pages.AlbumListPage
import musicstore.pages.AlbumShowPage
import net.sf.ehcache.Ehcache
import org.junit.After
import org.junit.Test
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.hasItem

class StaticScaffoldingCachingTests extends AbstractContentCachingTestCase {

	Ehcache albumControllerCache

	@After void tearDown() {
		Album.withTransaction {
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		super.tearDown()
	}

	@Test void openingListPageWithEmptyCache() {
		AlbumListPage.open()

		assertThat "cache hits", albumControllerCache.statistics.cacheHits, equalTo(0L)
		assertThat "cache misses", albumControllerCache.statistics.cacheMisses, equalTo(1L)
	}

	@Test void reloadingListPageHitsCache() {
		def page = AlbumListPage.open()

		page.refresh()

		assertThat "cache hits", albumControllerCache.statistics.cacheHits, equalTo(1L)
		assertThat "cache misses", albumControllerCache.statistics.cacheMisses, equalTo(1L)
	}

	@Test void saveFlushesCache() {
		def listPage = AlbumListPage.open()
		assertThat "initial page content", listPage.rowCount, equalTo(0)

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Edward Sharpe & the Magnetic Zeros"
		createPage.name = "Up From Below"
		createPage.year = "2009"
		createPage.save()

		assertThat "albums in database", Album.count(), equalTo(1)

		listPage = AlbumListPage.open()
		assertThat "updated page content", listPage.rowCount, equalTo(1)

		assertThat "cache hits", albumControllerCache.statistics.cacheHits, equalTo(0L)
		assertThat "cache misses", albumControllerCache.statistics.cacheMisses, equalTo(3L) // 2 misses on list page, 1 on show
		assertThat "cache size", albumControllerCache.statistics.objectCount, equalTo(2L) // show and list pages cached
	}

	@Test void failedSaveStillFlushesCache() {
		def listPage = AlbumListPage.open()
		assertThat "initial page content", listPage.rowCount, equalTo(0)

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Edward Sharpe & the Magnetic Zeros"
		createPage.name = ""
		createPage.year = "2009"
		createPage.saveExpectingFailure()

		assertThat "error messages", createPage.errorMessages, hasItem("Property [name] of class [class musicstore.Album] cannot be blank")
		assertThat "albums in database", Album.count(), equalTo(0)

		assertThat "cache size", albumControllerCache.statistics.objectCount, equalTo(0L) // cache was flushed even though save failed
	}

	@Test void differentShowPagesCachedSeparately() {
		def artist = Artist.build(name: "Metric")
		def album1 = Album.build(artist: artist, name: "Fantasies", year: "2009")
		def album2 = Album.build(artist: artist, name: "Live It Out", year: "2005")

		def showPage1 = AlbumShowPage.open(album1.id)
		assertThat "album name", showPage1.Name, equalTo(album1.name)

		def showPage2 = AlbumShowPage.open(album2.id)
		assertThat "album name", showPage2.Name, equalTo(album2.name)

		assertThat "cache hits", albumControllerCache.statistics.cacheHits, equalTo(0L)
		assertThat "cache misses", albumControllerCache.statistics.cacheMisses, equalTo(2L)
		assertThat "cache size", albumControllerCache.statistics.objectCount, equalTo(2L)
	}

	@Test void notFoundDoesNotGetCached() {
		def page = AlbumShowPage.openInvalidId(404)
		assertThat "flash message", page.flashMessage, equalTo("Album not found with id 404")

		// cache size will be 1 as list page is returned
		assertThat "cache size", albumControllerCache.statistics.objectCount, equalTo(1L)
	}
}

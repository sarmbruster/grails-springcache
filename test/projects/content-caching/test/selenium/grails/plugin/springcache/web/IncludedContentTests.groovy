package grails.plugin.springcache.web

import musicstore.pages.HomePage
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import musicstore.pages.AlbumCreatePage
import org.grails.rateable.Rating
import org.grails.rateable.RatingLink

import musicstore.pages.AlbumShowPage

class IncludedContentTests extends AbstractContentCachingTestCase {

	Ehcache latestControllerCache
	Ehcache popularControllerCache
	Album album1, album2, album3

	void setUp() {
		super.setUp()

		album1 = Album.build(artist: Artist.build(name: "Edward Sharpe & the Magnetic Zeros"), name: "Up From Below", year: "2009")
		album2 = Album.build(artist: Artist.build(name: "Yeasayer"), name: "Odd Blood", year: "2010")
		album3 = Album.build(artist: Artist.build(name: "Yeah Yeah Yeahs"), name: "It's Blitz!", year: "2009")
	}

	void tearDown() {
		logout()
		tearDownUsers()
		Album.withTransaction {tx ->
			RatingLink.list()*.delete()
			Rating.list()*.delete()
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		super.tearDown()
	}

	void testIncludedContentIsCached() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		def page = HomePage.open()
		assertEquals expectedList, page.latestAlbums

		page = page.refresh()
		assertEquals expectedList, page.latestAlbums

		assertEquals "cache misses", 1, latestControllerCache.statistics.cacheMisses
		assertEquals "cache hits", 1, latestControllerCache.statistics.cacheHits
	}

	void testIncludedContentCanBeFlushedByAnotherController() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		assertEquals expectedList, HomePage.open().latestAlbums

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Mumford & Sons"
		createPage.name = "Sigh No More"
		createPage.year = "2009"
		createPage.save()

		expectedList.add(0, "Sigh No More by Mumford & Sons (2009)")
		assertEquals expectedList, HomePage.open().latestAlbums
	}

	void testMultipleIncludesAreCachedSeparately() {
		def user = setUpUser("blackbeard", "Edward Teach")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		def expectedLatestList = [album3, album2, album1].collect { it.toString() }
		def expectedPopularList = [album1, album3, album2].collect { it.toString() }

		def page = HomePage.open()
		assertEquals expectedLatestList, page.latestAlbums
		assertEquals expectedPopularList, page.popularAlbums

		assertEquals "'latest' cache size", 1, latestControllerCache.statistics.objectCount
		assertEquals "'latest' cache misses", 1, latestControllerCache.statistics.cacheMisses
		assertEquals "'latest' cache hits", 0, latestControllerCache.statistics.cacheHits
		assertEquals "'popular' cache size", 1, popularControllerCache.statistics.objectCount
		assertEquals "'popular' cache misses", 1, popularControllerCache.statistics.cacheMisses
		assertEquals "'popular' cache hits", 0, popularControllerCache.statistics.cacheHits
	}

	void testIncludedContentFlushedByRateable() {
		setUpUser("ponytail", "Steven Segal")
		def user = setUpUser("roundhouse", "Chuck Norris")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 2.0)
		setUpAlbumRating(album3, user, 1.0)

		def expectedPopularList = [album1, album2, album3].collect { it.toString() }

		def homePage = loginAs("ponytail")
		assertEquals expectedPopularList, homePage.popularAlbums

		def showPage = AlbumShowPage.open(album3.id)
		showPage.vote 5

		expectedPopularList = [album3, album1, album2].collect { it.toString() }

		homePage = HomePage.open()
		assertEquals(expectedPopularList, homePage.popularAlbums)

		assertEquals "cache misses", 2, popularControllerCache.statistics.cacheMisses
	}

}
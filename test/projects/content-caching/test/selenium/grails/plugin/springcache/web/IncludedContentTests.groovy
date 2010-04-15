package grails.plugin.springcache.web

import musicstore.pages.HomePage
import musicstore.Album
import musicstore.Artist
import net.sf.ehcache.Ehcache
import musicstore.pages.AlbumCreatePage
import org.grails.rateable.Rating
import org.grails.rateable.RatingLink
import musicstore.pages.AlbumShowPage
import org.junit.*
import static org.junit.Assert.*
import static org.hamcrest.CoreMatchers.*

class IncludedContentTests extends AbstractContentCachingTestCase {

	Ehcache latestControllerCache
	Ehcache popularControllerCache
	Album album1, album2, album3

	@Before
	void setUp() {
		album1 = Album.build(artist: Artist.build(name: "Edward Sharpe & the Magnetic Zeros"), name: "Up From Below", year: "2009")
		album2 = Album.build(artist: Artist.build(name: "Yeasayer"), name: "Odd Blood", year: "2010")
		album3 = Album.build(artist: Artist.build(name: "Yeah Yeah Yeahs"), name: "It's Blitz!", year: "2009")
	}

	@After
	void tearDown() {
		logout()
		tearDownUsers()
		Album.withTransaction {tx ->
			RatingLink.list()*.delete()
			Rating.list()*.delete()
			Album.list()*.delete()
			Artist.list()*.delete()
		}
		clearCaches()
	}

	@Test
	void includedContentIsCached() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		def page = HomePage.open()
		assertThat "uncached content", page.latestAlbums, equalTo(expectedList)

		page = page.refresh()
		assertThat "cached content", page.latestAlbums, equalTo(expectedList)

		assertThat "cache hits", latestControllerCache.statistics.cacheMisses, equalTo(1L)
		assertThat "cache misses", latestControllerCache.statistics.cacheHits, equalTo(1L)
	}

	@Test
	void includedContentCanBeFlushedByAnotherController() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		assertThat "initial page content", HomePage.open().latestAlbums, equalTo(expectedList)

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Mumford & Sons"
		createPage.name = "Sigh No More"
		createPage.year = "2009"
		createPage.save()

		expectedList.add(0, "Sigh No More by Mumford & Sons (2009)")
		assertThat "updated page content", HomePage.open().latestAlbums, equalTo(expectedList)
	}

	@Test
	void multipleIncludesAreCachedSeparately() {
		def user = setUpUser("blackbeard", "Edward Teach")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		def expectedLatestList = [album3, album2, album1].collect { it.toString() }
		def expectedPopularList = [album1, album3, album2].collect { it.toString() }

		def page = HomePage.open()
		assertThat "latest albums list", page.latestAlbums, equalTo(expectedLatestList)
		assertThat "popular albums list", page.popularAlbums, equalTo(expectedPopularList)

		assertThat "latest albums cache size", latestControllerCache.statistics.objectCount, equalTo(1L)
		assertThat "latest albums cache misses", latestControllerCache.statistics.cacheMisses, equalTo(1L)
		assertThat "latest albums cache hits", latestControllerCache.statistics.cacheHits, equalTo(0L)
		assertThat "popular albums cache size", popularControllerCache.statistics.objectCount, equalTo(1L)
		assertThat "popular albums cache misses", popularControllerCache.statistics.cacheMisses, equalTo(1L)
		assertThat "popular albums cache hits", popularControllerCache.statistics.cacheHits, equalTo(0L)
	}

	@Test
	void includedContentFlushedByRateable() {
		setUpUser("ponytail", "Steven Segal")
		def user = setUpUser("roundhouse", "Chuck Norris")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 2.0)
		setUpAlbumRating(album3, user, 1.0)

		def expectedPopularList = [album1, album2, album3].collect { it.toString() }

		def homePage = loginAs("ponytail")
		assertThat "initial page content", homePage.popularAlbums, equalTo(expectedPopularList)

		def showPage = AlbumShowPage.open(album3.id)
		showPage.vote 5

		expectedPopularList = [album3, album1, album2].collect { it.toString() }

		homePage = HomePage.open()
		assertThat "updated page content", homePage.popularAlbums, equalTo(expectedPopularList)

		assertThat "cache misses", popularControllerCache.statistics.cacheMisses, equalTo(2L)
	}

}
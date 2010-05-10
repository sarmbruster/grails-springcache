package grails.plugin.springcache.web

import musicstore.Album
import musicstore.Artist
import musicstore.pages.AlbumCreatePage
import musicstore.pages.AlbumShowPage
import musicstore.pages.HomePage
import net.sf.ehcache.Ehcache
import org.grails.rateable.Rating
import org.grails.rateable.RatingLink
import static grails.plugin.springcache.matchers.CacheHitsMatcher.hasCacheHits
import static grails.plugin.springcache.matchers.CacheMissesMatcher.hasCacheMisses
import static grails.plugin.springcache.matchers.CacheSizeMatcher.hasCacheSize
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo

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
		assertThat "Latest albums", page.latestAlbums, equalTo(expectedList)

		page = page.refresh()
		assertThat "Latest albums", page.latestAlbums, equalTo(expectedList)

		assertThat latestControllerCache, hasCacheMisses(1)
		assertThat latestControllerCache, hasCacheHits(1)
	}

	void testIncludedContentCanBeFlushedByAnotherController() {
		def expectedList = [album3, album2, album1].collect { it.toString() }
		assertThat "Latest albums", HomePage.open().latestAlbums, equalTo(expectedList)

		def createPage = AlbumCreatePage.open()
		createPage.artist = "Mumford & Sons"
		createPage.name = "Sigh No More"
		createPage.year = "2009"
		createPage.save()

		expectedList.add(0, "Sigh No More by Mumford & Sons (2009)")
		assertThat "Latest albums", HomePage.open().latestAlbums, equalTo(expectedList)
	}

	void testMultipleIncludesAreCachedSeparately() {
		def user = setUpUser("blackbeard", "Edward Teach")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		def expectedLatestList = [album3, album2, album1].collect { it.toString() }
		def expectedPopularList = [album1, album3, album2].collect { it.toString() }

		def page = HomePage.open()
		assertThat "Latest albums", page.latestAlbums, equalTo(expectedLatestList)
		assertThat "Popular albums", page.popularAlbums, equalTo(expectedPopularList)

		assertThat "Latest albums cache", latestControllerCache, hasCacheSize(1)
		assertThat "Latest albums cache", latestControllerCache, hasCacheMisses(1)
		assertThat "Latest albums cache", latestControllerCache, hasCacheHits(0)
		assertThat "Popular albums cache", popularControllerCache, hasCacheSize(1)
		assertThat "Popular albums cache", popularControllerCache, hasCacheMisses(1)
		assertThat "Popular albums cache", popularControllerCache, hasCacheHits(0)
	}

	void testIncludedContentFlushedByRateable() {
		setUpUser("ponytail", "Steven Segal")
		def user = setUpUser("roundhouse", "Chuck Norris")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 2.0)
		setUpAlbumRating(album3, user, 1.0)

		def expectedPopularList = [album1, album2, album3].collect { it.toString() }

		def homePage = loginAs("ponytail")
		assertThat "Popular albums", homePage.popularAlbums, equalTo(expectedPopularList)

		def showPage = AlbumShowPage.open(album3.id)
		showPage.vote 5

		expectedPopularList = [album3, album1, album2].collect { it.toString() }

		homePage = HomePage.open()
		assertThat "Popular albums", homePage.popularAlbums, equalTo(expectedPopularList)

		assertThat popularControllerCache, hasCacheMisses(3) // Selenium GET on nav from login, then GET + HEAD on open
	}

}
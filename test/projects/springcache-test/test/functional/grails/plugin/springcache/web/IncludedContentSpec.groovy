package grails.plugin.springcache.web

import spock.lang.*
import musicstore.*
import musicstore.pages.*
import net.sf.ehcache.Ehcache
import org.grails.rateable.*
import org.codehaus.groovy.grails.commons.ApplicationHolder

class IncludedContentSpec extends AbstractContentCachingSpec {

	@Shared Ehcache latestControllerCache = ApplicationHolder.application.mainContext.latestControllerCache
	@Shared Ehcache popularControllerCache = ApplicationHolder.application.mainContext.popularControllerCache
	
	Album album1, album2, album3

	def setup() {
		Album.withNewSession {
			album1 = Album.build(artist: Artist.build(name: "Edward Sharpe & the Magnetic Zeros"), name: "Up From Below", year: "2009")
			album2 = Album.build(artist: Artist.build(name: "Yeasayer"), name: "Odd Blood", year: "2010")
			album3 = Album.build(artist: Artist.build(name: "Yeah Yeah Yeahs"), name: "It's Blitz!", year: "2009")
		}
	}

	def cleanup() {
		logout()
		tearDownUsers()
		Album.withNewSession {
			RatingLink.list()*.delete()
			Rating.list()*.delete()
			Album.list()*.delete()
			Artist.list()*.delete()
		}
	}

	def "included content is cached"() {
		given: "I am on the home page"
		to HomePage

		when: "I refresh the page"
		driver.navigate().refresh()
		
		then: "the same content is displayed"
		latestAlbums == [album3, album2, album1]*.toString()
		latestAlbums == old(latestAlbums)

		and: "the cache was hit the second time the page loaded"
		latestControllerCache.statistics.cacheMisses == 1L
		latestControllerCache.statistics.cacheHits == 1L
	}

	def "included content can be flushed by another controller"() {
		given: "The latest albums module is cached"
		to HomePage
		
		when: "I create a new album"
		to AlbumCreatePage
		albumForm.artist = "Mumford & Sons"
		albumForm.name = "Sigh No More"
		albumForm.year = "2009"
		createButton.click()
		
		and: "I return to the home page"
		to HomePage

		then: "the latest albums cache was flushed"
		latestControllerCache.statistics.cacheHits == 0L
		latestControllerCache.statistics.cacheMisses == 2L
		
		and: "the new album appears in the latest list"
		latestAlbums == ["Sigh No More by Mumford & Sons (2009)"] + old(latestAlbums)
	}

	def "multiple includes are cached separately"() {
		given: "some ratings exist"
		def user = setUpUser("blackbeard", "Edward Teach")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		when: "I visit the home page"
		to HomePage
		
		then: "I see the popular and latest album lists"
		latestAlbums == [album3, album2, album1]*.toString()
		popularAlbums == [album1, album3, album2]*.toString()

		and: "their content is cached separately"
		latestControllerCache.statistics.objectCount == 1L
		latestControllerCache.statistics.cacheMisses == 1L
		latestControllerCache.statistics.cacheHits == 0L
		popularControllerCache.statistics.objectCount == 1L
		popularControllerCache.statistics.cacheMisses == 1L
		popularControllerCache.statistics.cacheHits == 0L
	}

	def "included content flushed by rateable"() {
		given: "some ratings exist"
		setUpUser("ponytail", "Steven Segal")
		def user = setUpUser("roundhouse", "Chuck Norris")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 2.0)
		setUpAlbumRating(album3, user, 1.0)

		and: "the popular albums module is cached"
		to HomePage
		
		when: "I rate an album"
		to LoginPage
		loginAs("ponytail")
		to AlbumShowPage, album3.id
		vote 5
		
		and: "I return to the home page"
		to HomePage
		
		then: "the cache has not been hit"
		popularControllerCache.statistics.cacheHits == 0L
		popularControllerCache.statistics.cacheMisses == 2L

		and: "the popular albums are listed a different order"
		old(popularAlbums) == [album1, album2, album3]*.toString()
		popularAlbums == [album3, album1, album2]*.toString()
	}

}
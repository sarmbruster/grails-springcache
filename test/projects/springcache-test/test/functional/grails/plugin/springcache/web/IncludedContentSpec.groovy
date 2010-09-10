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
		given:
		def expectedList = [album3, album2, album1]*.toString()
		
		when:
		to HomePage
		
		then:
		latestAlbums == expectedList

		when:
		driver.navigate().refresh()
		
		then:
		latestAlbums == expectedList

		and:
		latestControllerCache.statistics.cacheMisses == 1L
		latestControllerCache.statistics.cacheHits == 1L
	}

	def "included content can be flushed by another controller"() {
		given:
		def expectedList = [album3, album2, album1]*.toString()
		
		when:
		to HomePage
		
		then:
		latestAlbums == expectedList

		when:
		to AlbumCreatePage
		albumForm.artist = "Mumford & Sons"
		albumForm.name = "Sigh No More"
		albumForm.year = "2009"
		createButton.click()
		
		to HomePage
		
		then:
		latestAlbums == ["Sigh No More by Mumford & Sons (2009)"] + old(latestAlbums)
	}

	def "multiple includes are cached separately"() {
		given:
		def user = setUpUser("blackbeard", "Edward Teach")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 3.0)
		setUpAlbumRating(album3, user, 4.0)

		when:
		to HomePage
		
		then:
		latestAlbums == [album3, album2, album1]*.toString()
		popularAlbums == [album1, album3, album2]*.toString()

		and:
		latestControllerCache.statistics.objectCount == 1L
		latestControllerCache.statistics.cacheMisses == 1L
		latestControllerCache.statistics.cacheHits == 0L
		popularControllerCache.statistics.objectCount == 1L
		popularControllerCache.statistics.cacheMisses == 1L
		popularControllerCache.statistics.cacheHits == 0L
	}

	def "included content flushed by rateable"() {
		given:
		setUpUser("ponytail", "Steven Segal")
		def user = setUpUser("roundhouse", "Chuck Norris")
		setUpAlbumRating(album1, user, 5.0)
		setUpAlbumRating(album2, user, 2.0)
		setUpAlbumRating(album3, user, 1.0)

		when:
		to LoginPage
		loginAs("ponytail")
		
		then: "the popular list should contain the expected data"
		popularAlbums == [album1, album2, album3]*.toString()

		when: "voting on the show page should flush the popular list's cache"
		to AlbumShowPage, album3.id
		vote 5
		to HomePage
		
		then: "the cache has been flushed so the list should appear in a different order"
		popularAlbums = [album3, album1, album2]*.toString()

		and:
		popularControllerCache.statistics.cacheMisses == 2L
	}

}
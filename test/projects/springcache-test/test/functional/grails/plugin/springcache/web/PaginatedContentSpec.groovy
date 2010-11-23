package grails.plugin.springcache.web

import grails.plugin.geb.GebSpec
import grails.plugin.springcache.SpringcacheService
import musicstore.pages.AlbumListPage
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import musicstore.*
import spock.lang.*

@Stepwise
class PaginatedContentSpec extends GebSpec {
	
	@Shared SpringcacheService springcacheService = ApplicationHolder.application.mainContext.springcacheService
	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache

	def setupSpec() {
		Album.withNewSession {
			Album.build(artist: new Artist(name: "Les Savy Fav"), name: "Root For Ruin", year: "2010")
			Album.build(artist: new Artist(name: "Dangermouse & Sparklehorse"), name: "Dark Night of the Soul", year: "2010")
			Album.build(artist: new Artist(name: "Arcade Fire"), name: "The Suburbs", year: "2010")
			Album.build(artist: new Artist(name: "Wolf Parade"), name: "Expo '86", year: "2010")
			Album.build(artist: new Artist(name: "The National"), name: "High Violet", year: "2010")
			Album.build(artist: new Artist(name: "The Hold Steady"), name: "Heaven is Whenever", year: "2010")
			Album.build(artist: new Artist(name: "Yeasayer"), name: "Odd Blood", year: "2010")
			Album.build(artist: new Artist(name: "Vampire Weekend"), name: "Contra", year: "2010")
			Album.build(artist: new Artist(name: "Emmy the Great"), name: "First Love", year: "2009")
			Album.build(artist: new Artist(name: "Mumford & Sons"), name: "Sigh No More", year: "2009")
			Album.build(artist: new Artist(name: "The XX"), name: "XX", year: "2009")
		}
	}

	def cleanupSpec() {
		Album.withNewSession {
			Album.list()*.delete()
			Artist.list()*.delete()
		}

		springcacheService.flushAll()
		springcacheService.clearStatistics()
	}
	
	def "loading the first list page primes the cache"() {
		when: "I visit the album list page"
		to AlbumListPage
		
		then: "the correct content is displayed"
		rows.size() == 10
		
		and: "the cache is missed"
		cacheHits == 0
		cacheMisses == old(cacheMisses) + 1
		cacheSize == old(cacheSize) + 1
	}

	def "navigating to the second page does not display cached content"() {
		when: "I navigate to the 2nd page"
		paginationModule.nextLink.click()
		
		then: "the correct content is displayed"
		rows.size() == 1
		
		and: "the page is not served from the cache"
		cacheHits == 0
		cacheMisses == old(cacheMisses) + 1
		cacheSize == old(cacheSize) + 1
	}
	
	def "returning to the first page misses the cache as query string parameters are present"() {
		when: "I navigate back to the 1st page"
		paginationModule.prevLink.click()
		
		then: "the correct content is displayed"
		rows.size() == 10
		
		and: "the page is not served from the cache"
		cacheMisses == old(cacheMisses) + 1
	}
	
	def "returning to the second page serves content from the cache"() {
		when: "I navigate back to the 2st page"
		paginationModule.nextLink.click()
		
		then: "the correct content is displayed"
		rows.size() == 1
		
		and: "the page is served from the cache"
		cacheHits == old(cacheHits) + 1
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
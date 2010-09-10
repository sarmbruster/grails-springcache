package grails.plugin.springcache.web

import spock.lang.*
import musicstore.pages.*
import net.sf.ehcache.Ehcache
import grails.plugin.geb.*
import musicstore.pages.*
import org.codehaus.groovy.grails.commons.ApplicationHolder

class AuthenticatedContentSpec extends AbstractContentCachingSpec {

	@Shared Ehcache albumControllerCache = ApplicationHolder.application.mainContext.albumControllerCache
	@Shared Ehcache userControllerCache = ApplicationHolder.application.mainContext.userControllerCache

	def setup() {
		setUpUser "blackbeard", "Edward Teach"
	}

	def cleanup() {
		logout()
		tearDownUsers()
	}

	def "login state is displayed on uncached page"() {
		given: "I am not logged in when I visit the homepage"
		to HomePage

		when: "I log in"
		to LoginPage
		loginAs "blackbeard"

		then: "I am on the home page and now logged in"
		at HomePage
		!old(loggedIn)
		loggedIn
		loggedInMessage == "Logged in as blackbeard"
	}

	def "login state is not cached in the page"() {
		given: "I am not logged in when I visit the album list page"
		to AlbumListPage

		when: "I log in"
		to LoginPage
		loginAs "blackbeard"
		to AlbumListPage

		then: "the page content is retrieved from the cache"
		albumControllerCache.statistics.cacheHits == old(albumControllerCache.statistics.cacheHits) + 1

		and: "the authentication message is displayed"
		!old(loggedIn)
		loggedIn
		loggedInMessage == "Logged in as blackbeard"
	}

	def "non success responses are not cached"() {
		when:
		to UserListPage
		page LoginPage

		then:
		at LoginPage
		userControllerCache.statistics.objectCount == 0L
	}

}
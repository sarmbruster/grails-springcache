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
		when:
		to HomePage

		then:
		!isLoggedIn()

		when:
		to LoginPage
		loginAs "blackbeard"

		then:
		at HomePage
		isLoggedIn()
		loggedInMessage == "Logged in as blackbeard"
	}

	def "login state is not cached in the page"() {
		when:
		to AlbumListPage

		then:
		!isLoggedIn()

		when:
		to LoginPage
		loginAs "blackbeard"
		to AlbumListPage

		then:
		isLoggedIn()
		loggedInMessage == "Logged in as blackbeard"
	}

	def "non success responses are not cached"() {
		when:
		to UserListPage
		page LoginPage

		then:
		at LoginPage
		userControllerCache.statistics.objectCount == 0L

		when:
		loginAs "blackbeard", "password", UserListPage

		then:
		at UserListPage
		userControllerCache.statistics.objectCount == 1L
	}

}
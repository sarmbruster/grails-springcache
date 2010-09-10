package grails.plugin.springcache.web

import musicstore.pages.AlbumListPage
import musicstore.pages.HomePage
import musicstore.pages.UserListPage
import net.sf.ehcache.Ehcache
import static grails.plugin.springcache.matchers.CacheHitsMatcher.hasCacheHits
import static grails.plugin.springcache.matchers.CacheSizeMatcher.hasCacheSize
import static grails.plugin.springcache.matchers.CacheSizeMatcher.isEmptyCache
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import grails.plugin.geb.GebSpec
import musicstore.auth.User
import musicstore.pages.LoginPage

class AuthenticatedContentSpec extends AbstractContentCachingSpec {

	def authenticateService
	def fixtureLoader
	Ehcache albumControllerCache
	Ehcache userControllerCache

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

		then:
		at LoginPage
		userControllerCache.statistics.objectCount == 0L

		when:
		loginAs "blackbeard"

		then:
		at UserListPage
		userControllerCache.statistics.objectCount == 1L
	}

}
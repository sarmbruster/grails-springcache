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

class AuthenticatedContentTests extends AbstractContentCachingTestCase {

	Ehcache albumControllerCache
	Ehcache userControllerCache

	void setUp() {
		super.setUp()
		setUpUser("blackbeard", "Edward Teach")
	}

	void tearDown() {
		logout()
		tearDownUsers()
		super.tearDown()
	}

	void testLoginOnUncachedPage() {
		def page = HomePage.open()
		assertFalse "User should not be logged in", page.isUserLoggedIn()

		page = loginAs("blackbeard")

		assertTrue "User should now be logged in", page.isUserLoggedIn()
		assertThat "Logged in user message", page.loggedInMessage, equalTo("Logged in as blackbeard")
	}

	void testLoginStateNotCachedWithPage() {
		def listPage = AlbumListPage.open()
		assertFalse "User should not be logged in", listPage.isUserLoggedIn()

		loginAs "blackbeard"

		listPage = AlbumListPage.open()
		assertThat "Logged in user message", listPage.loggedInMessage, equalTo("Logged in as blackbeard")

		assertThat albumControllerCache, hasCacheHits(2) // Selenium HEAD + GET
	}

	void testCachingOfAuthenticatedAction() {
		def loginPage = UserListPage.openNotAuthenticated()
		assertThat "Page should not be cached if status is 403", userControllerCache, isEmptyCache()

		loginPage.j_username = "blackbeard"
		loginPage.j_password = "password"
		loginPage.login(UserListPage)
		
		assertThat userControllerCache, hasCacheSize(1)

		logout()
		UserListPage.openNotAuthenticated()
	}

}
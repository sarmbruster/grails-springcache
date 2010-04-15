package grails.plugin.springcache.web

import musicstore.pages.AlbumListPage
import musicstore.pages.HomePage
import musicstore.pages.UserListPage
import net.sf.ehcache.Ehcache
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.AfterClass

class AuthenticatedContentTests extends AbstractContentCachingTestCase {

	Ehcache albumControllerCache
	Ehcache userControllerCache

	@Before
	void setUp() {
		setUpUser("blackbeard", "Edward Teach")
	}

	@After
	void tearDown() {
		logout()
		tearDownUsers()
		super.tearDown()
	}

	@Test
	void loginOnUncachedPage() {
		def page = HomePage.open()
		assertFalse "User should not be logged in", page.isUserLoggedIn()

		page = loginAs("blackbeard")

		assertTrue "User should now be logged in", page.isUserLoggedIn()
		assertThat "logged in message", page.loggedInMessage, equalTo("Logged in as blackbeard")
	}

	@Test
	void loginStateNotCachedWithPage() {
		def listPage = AlbumListPage.open()
		assertFalse "User should not be logged in", listPage.isUserLoggedIn()

		loginAs "blackbeard"

		listPage = AlbumListPage.open()
		assertThat "logged in message", listPage.loggedInMessage, equalTo("Logged in as blackbeard")

		assertThat "cache hits", albumControllerCache.statistics.cacheHits, equalTo(1L)
	}

	@Test
	void cachingOfAuthenticatedAction() {
		def loginPage = UserListPage.openNotAuthenticated()
		assertThat "Page should not be cached if status is 403", userControllerCache.statistics.objectCount, equalTo(0L)

		loginPage.j_username = "blackbeard"
		loginPage.j_password = "password"
		loginPage.login(UserListPage)

		assertThat "cache size", userControllerCache.statistics.objectCount, equalTo(1L)

		logout()
		UserListPage.openNotAuthenticated()
	}

}
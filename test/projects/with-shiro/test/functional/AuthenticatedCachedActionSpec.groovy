import geb.Page
import grails.plugin.geb.GebSpec
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class AuthenticatedCachedActionSpec extends GebSpec {

	@Shared Ehcache pirateCache = ApplicationHolder.application.mainContext.pirateCache

	def cleanupSpec() {
		pirateCache.removeAll()
		pirateCache.clearStatistics()
	}

	def "the list page requires the user to log in"() {
		when: "I try to hit the list page"
		to PirateListPage
		page LoginPage

		then: "I must log in"
		at LoginPage
	}

	def "a logged in user can view the list page"() {
		when: "I have logged in"
		form.username = "blackbeard"
		form.password = "password"
		loginButton.click(PirateListPage)

		then: "the list page is displayed"
		at PirateListPage
		pirateNames == ShiroUser.list().name

		and: "the content was not served from the cache"
		pirateCache.statistics.cacheMisses == old(pirateCache.statistics.cacheMisses) + 1
	}

	def "content behind the authentication filter can be cached"() {
		when: "I refresh the list page"
		driver.navigate().refresh()

		then: "the list page is displayed"
		pirateNames == ShiroUser.list().name

		and: "the content was served from the cache"
		pirateCache.statistics.cacheHits == old(pirateCache.statistics.cacheHits) + 1
	}

}

class LoginPage extends Page {

	static url = "/auth/login"
	static at = { title == "Login" }
	static content = {
		form { $("form") }
		loginButton { $("input[type=submit]") }
	}

}

class PirateListPage extends Page {

	static url = "/pirate/list"
	static at = { title == "List of Fearsome Pirates" }
	static content = {
		pirateNames { $("li")*.text() }
	}

}

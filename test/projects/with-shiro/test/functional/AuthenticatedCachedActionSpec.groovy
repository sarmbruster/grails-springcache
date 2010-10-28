import grails.plugin.geb.GebSpec
import spock.lang.Stepwise
import geb.Page

@Stepwise
class AuthenticatedCachedActionSpec extends GebSpec {

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
		pirateNames == ShiroUser.list().username

		and: "the content was not served from the cache"
	}

	def "content behind the authentication filter can be cached"() {
		when: "I refresh the list page"
		driver.navigate().refresh()

		then: "the list page is displayed"
		pirateNames == ShiroUser.list().username

		and: "the content was served from the cache"
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

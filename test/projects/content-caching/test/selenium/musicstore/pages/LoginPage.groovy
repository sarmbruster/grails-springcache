package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsFormPage
import grails.plugins.selenium.pageobjects.GrailsPage
import grails.plugins.selenium.pageobjects.UnexpectedPageException

class LoginPage extends GrailsFormPage {

	static LoginPage open() {
		return new LoginPage("/login")
	}

	LoginPage() {
		super()
	}

	// TODO: this shouldn't really be public
	LoginPage(String uri) {
		super(uri)
	}

	GrailsPage login(Class<? extends GrailsPage> expectedPageType = HomePage) {
		selenium.clickAndWait("css=#loginForm input[type=submit]")
		if (selenium.isElementPresent("css=.login_message")) {
			def loginMessage = selenium.getText("css=.login_message")
			throw new UnexpectedPageException("Login failed with message: '$loginMessage'")
		}
		return expectedPageType.newInstance()
	}

	@Override protected void verifyPage() {
		pageTitleIs "Login"
	}
}
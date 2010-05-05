package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsPage

class NotFoundPage extends GrailsPage {

	NotFoundPage() {
		super()
	}

	void verifyPage() {
		pageTitleIs "404: Not Found"
	}
}

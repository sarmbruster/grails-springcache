package musicstore.pages

import geb.Page

class HomePage extends Page {

	static url = "/"
	static at = { title == "Welcome to Grails" }

	static content = {
		loggedInMessage(required: false) { $("#loggedInUser").text() }
		loginLink(required: false, to: LoginPage) { $("#loginLink a") }
		latestAlbums { $("#latestAlbums ol li")*.text() }
		popularAlbums { $("#popularAlbums ol li .album")*.text() }
	}

	boolean isLoggedIn() {
		loginLink.empty
	}

}
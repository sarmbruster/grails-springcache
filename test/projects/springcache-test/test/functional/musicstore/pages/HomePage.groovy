package musicstore.pages

import geb.Page

class HomePage extends Page {

	static url = "/"
	static at = { title == "Welcome to Grails" }

	static content = {
		authModule { module AuthModule }
		latestAlbums { $("#latestAlbums ol li")*.text() }
		popularAlbums { $("#popularAlbums ol li .album")*.text() }
	}

}
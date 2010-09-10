package musicstore.pages

import geb.Page

class AlbumListPage extends Page {

	static url = "/album/list"
	static at = { title == "Album List" }

	static content = {
		rows(required: false) { $("tbody tr") }
		flashMessage(required: false) { $(".message").text() }
		loggedInMessage(required: false) { $("#loggedInUser").text() } // TODO: duplicated in several pages
	}

	boolean isLoggedIn() {
		loggedInMessage != null
	}

}

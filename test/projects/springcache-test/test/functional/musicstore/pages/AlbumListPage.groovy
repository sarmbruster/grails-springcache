package musicstore.pages

import geb.Page

class AlbumListPage extends Page {

	static url = "/album/list"
	static at = { title == "Album List" }

	static content = {
		loggedInMessage(required: false) { $("#loggedInUser").text() } // TODO: duplicated in several pages
		loginLink(required: false, to: LoginPage) { $("#loginLink a") }
		rows(required: false) { $("tbody tr") }
		flashMessage(required: false) { $(".message").text() }
	}

	boolean isLoggedIn() {
		loginLink.empty
	}

}

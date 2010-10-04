package musicstore.pages

import geb.Page

class AlbumListPage extends Page {

	static url = "/album/list"
	static at = { title == "Album List" }

	static content = {
		authModule { module AuthModule }
		rows(required: false) { $("tbody tr") }
		flashMessage(required: false) { $(".message").text() }
		paginationModule { module PaginationModule }
	}

}

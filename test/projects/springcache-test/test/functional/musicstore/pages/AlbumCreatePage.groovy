package musicstore.pages

import geb.Page

class AlbumCreatePage extends Page {

	static url = "/album/create"
	static at = { title == "Create Album" }
	
	static content = {
		albumForm { $("form") }
		createButton(to: AlbumShowPage) { albumForm.find("input.save") }
		errorMessages(required: false) { $(".errors ul li")*.text() }
	}

}
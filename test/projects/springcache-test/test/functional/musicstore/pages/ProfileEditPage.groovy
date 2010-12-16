package musicstore.pages

import geb.Page

class ProfileEditPage extends Page {

	static url = "/profile/edit"
	static at = { $("h1").text() == "Edit Your Profile" }

	static content = {
		profile { $("form") }
	}

}

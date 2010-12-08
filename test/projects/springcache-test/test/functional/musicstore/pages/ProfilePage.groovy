package musicstore.pages

import geb.Page

class ProfilePage extends Page {

	static url = "/profile"
	static at = { title.startsWith("Profile:") }
	
}

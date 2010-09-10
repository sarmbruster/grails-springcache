package musicstore.pages

import geb.Page

class ArtistListPage extends Page {

	static url = "/artist/list"
	static at = { title == "Artist List" }

	boolean isSitemeshDecorated() { $("css=#grailsLogo") != null }

}
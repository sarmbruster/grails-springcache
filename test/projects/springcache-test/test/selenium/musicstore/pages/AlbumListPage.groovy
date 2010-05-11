package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage

class AlbumListPage extends GrailsListPage {

	static AlbumListPage open() {
		return new AlbumListPage("/album/list")
	}

	AlbumListPage() {
		super()
	}

	AlbumListPage(String uri) {
		super(uri)
	}

	AlbumListPage refresh() {
		selenium.refresh()
		return new AlbumListPage()
	}

	String getTitle() { selenium.title }

	// TODO: sucks duplicating these from HomePage but I get StackOverflowError if I try to use @Mixin as classes have common root
	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

	@Override protected void verifyPage() {
		pageTitleIs "Album List"
	}

}

package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage

class ArtistListPage extends GrailsListPage {

	static ArtistListPage open() {
		return new ArtistListPage("/artist/list")
	}

	ArtistListPage() {
		super()
	}

	private ArtistListPage(String uri) {
		super(uri)
	}

	ArtistListPage refresh() {
		selenium.refresh()
		return new ArtistListPage()
	}

	String getTitle() { selenium.title }

	boolean isSitemeshDecorated() { selenium.isElementPresent "css=#grailsLogo" }

	@Override protected void verifyPage() {
		pageTitleIs "Artist List"
	}
}
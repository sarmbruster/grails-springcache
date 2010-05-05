package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsCreatePage

class AlbumCreatePage extends GrailsCreatePage {

	static AlbumCreatePage open() {
		return new AlbumCreatePage("/album/create")
	}

	AlbumCreatePage() {
		super()
	}

	private AlbumCreatePage(String uri) {
		super(uri)
	}

	@Override protected void verifyPage() {
		pageTitleIs "Create Album"
	}
}
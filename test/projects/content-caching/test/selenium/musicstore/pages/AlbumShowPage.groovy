package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsShowPage
import junit.framework.Assert
import static org.hamcrest.Matchers.*

class AlbumShowPage extends GrailsShowPage {

	static AlbumShowPage open(id) {
		return new AlbumShowPage("/album/show/$id")
	}

	static AlbumListPage openInvalidId(id) {
		return new AlbumListPage("/album/show/$id")
	}

	AlbumShowPage() {
		super()
	}

	private AlbumShowPage(String uri) {
		super(uri)
	}

	void vote(int stars) {
		if (stars in (1..5)) {
			selenium.click("rating_star_$stars")
			selenium.waitForText("rating_notifytext", startsWith("Rating saved"))
		} else {
			throw new IllegalArgumentException("Can only vote 1..5 stars")
		}
	}

	@Override protected void verifyPage() {
		pageTitleIs "Show Album"
	}
}
package musicstore.pages

import geb.Page
import java.util.concurrent.*

class AlbumShowPage extends Page {

	static url = "/album/show"
	static at = { title == "Show Album" }

	static content = {
		ratingStars { $(".rating") }
		ratingConfirmation(required: false) { $("#rating_notifytext").text() }
	}

	void rate(int stars) {
		if (stars in (1..5)) {
			ratingStars.find("#rating_star_$stars a").click()
			waitFor {
				ratingConfirmation =~ /^Rating saved\./
			}
		} else {
			throw new IllegalArgumentException("Can only vote 1..5 stars")
		}
	}
}
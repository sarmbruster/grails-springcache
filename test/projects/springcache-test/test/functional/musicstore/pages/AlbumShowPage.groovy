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
	
	private void waitFor(Closure condition) {
		def latch = new CountDownLatch(1)
		def thread = Thread.start {
			try {
				while (!condition()) TimeUnit.MILLISECONDS.sleep(250)
				latch.countDown()
			} catch (InterruptedException e) { }
		}
		try {
			assert latch.await(2, TimeUnit.SECONDS), "Timed out after 2 seconds"
		} finally {
			thread.interrupt()
		}
	}
}
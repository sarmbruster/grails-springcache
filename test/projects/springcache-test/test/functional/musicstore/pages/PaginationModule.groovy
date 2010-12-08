package musicstore.pages

import geb.Module

class PaginationModule extends Module {
	
	static content = {
		currentPage(required: false) { $(".paginateButtons .currentStep").text().toInteger() }
		prevLink(required: false) { $(".paginateButtons a.prevLink") }
		nextLink(required: false) { $(".paginateButtons a.nextLink") }
	}
	
	boolean isFirstPage() {
		currentPage == 1
	}
	
	boolean isLastPage() {
		nextLink.empty
	}

}
package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsPage

class HomePage extends GrailsPage {

	static HomePage open() {
		return new HomePage("/")
	}

	HomePage() {
		super()
	}

	private HomePage(String uri) {
		super(uri)
	}

	HomePage refresh() {
		selenium.refreshAndWait()
		return new HomePage()
	}

	String getLoggedInMessage() {
		return isUserLoggedIn() ? selenium.getText("loggedInUser") : null
	}

	boolean isUserLoggedIn() {
		selenium.isElementPresent("loggedInUser")
	}

	LoginPage goToLogin() {
		if (isUserLoggedIn()) {
			throw new IllegalStateException("Already logged in")
		} else {
			selenium.clickAndWait("css=#loginLink a")
			return new LoginPage()
		}
	}

	List<String> getLatestAlbums() {
		def list = []
		int i = 1
		while (selenium.isElementPresent("//div[@id='latestAlbums']/ol/li[$i]")) {
			list << selenium.getText("//div[@id='latestAlbums']/ol/li[$i]")
			i++
		}
		return list
	}

	List<String> getPopularAlbums() {
		def list = []
		int i = 1
		while (selenium.isElementPresent("//div[@id='popularAlbums']/ol/li[$i]")) {
			list << selenium.getText("//div[@id='popularAlbums']/ol/li[$i]/span[@class='album']")
			i++
		}
		return list
	}

	@Override protected void verifyPage() {
		pageTitleIs "Welcome to Grails"
	}
}
package musicstore.pages

import grails.plugins.selenium.pageobjects.GrailsListPage

class UserListPage extends GrailsListPage {

	static UserListPage open() {
		return new UserListPage("/user/list")
	}

	static LoginPage openNotAuthenticated() {
		return new LoginPage("/user/list")
	}

	UserListPage() {
		super()
	}

	private UserListPage(String uri) {
		super(uri)
	}

	@Override protected void verifyPage() {
		pageTitleIs "User List"
	}

}
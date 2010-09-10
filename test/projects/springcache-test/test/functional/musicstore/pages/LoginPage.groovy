package musicstore.pages

import geb.Page

class LoginPage extends Page {

	static url = "/login"
	static at = { title == "Login" }

	static content = {
		loginForm { $("#loginForm") }
		loginButton { loginForm.find("input[type=submit]") }
	}

	void loginAs(String username, String password = "password") {
		loginForm.j_username = username
		loginForm.j_password = password
		loginButton.click()
	}
}
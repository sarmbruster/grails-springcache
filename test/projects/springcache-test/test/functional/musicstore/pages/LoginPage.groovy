package musicstore.pages

import geb.Page

class LoginPage extends Page {

	static url = "/login"
	static at = { title == "Login" }

	static content = {
		loginForm { $("#loginForm") }
		loginButton(to: HomePage) { loginForm.find("input[type=submit]") }
	}

	void loginAs(String username, String password = "password", Class<? extends Page> page = HomePage) {
		loginForm.j_username = username
		loginForm.j_password = password
		loginButton.click(page)
	}
}
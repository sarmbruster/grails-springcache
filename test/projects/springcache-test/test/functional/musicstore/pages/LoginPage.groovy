package musicstore.pages

import geb.Page

class LoginPage extends Page {

	static url = "/auth/login"
	static at = { title == "Login" }

	static content = {
		loginForm { $("form") }
		loginButton(to: HomePage) { loginForm.find("input[type=submit]") }
	}

	void loginAs(String username, String password = "password", Class<? extends Page> page = HomePage) {
		loginForm.username = username
		loginForm.password = password
		loginButton.click(page)
	}
}
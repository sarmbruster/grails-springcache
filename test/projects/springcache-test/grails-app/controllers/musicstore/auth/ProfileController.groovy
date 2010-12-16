package musicstore.auth

import grails.plugin.springcache.annotations.Cacheable
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import auth.User
import org.apache.shiro.SecurityUtils
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.apache.shiro.crypto.hash.Sha256Hash

class ProfileController {

	static defaultAction = "display"
	static allowedMethods = [update: "POST"]

	@Cacheable(cache = "profileCache", cacheResolver = "profileCacheResolver")
	def display = {
		def user = currentUser
		if (user) {
			[userInstance: user]
		} else {
			response.sendError SC_UNAUTHORIZED
		}
	}

	@Cacheable(cache = "profileCache", cacheResolver = "profileCacheResolver")
	def edit = {
		def user = currentUser
		if (user) {
			[userInstance: user]
		} else {
			response.sendError SC_UNAUTHORIZED
		}
	}

	def update = {
		def user = currentUser
		user.properties = params
		if (params.password) {
			user.passwordHash = new Sha256Hash(params.password).toHex()
		}
		if (!user.hasErrors() && user.save(flush: true)) {
			redirect action: "display"
		} else {
			render view: "edit", model: [userInstance: user]
		}
	}

	private User getCurrentUser() {
		def principal = SecurityUtils.subject?.principal
		if (principal) {
			User.findByUsername(principal)
		} else {
			null
		}
	}
}

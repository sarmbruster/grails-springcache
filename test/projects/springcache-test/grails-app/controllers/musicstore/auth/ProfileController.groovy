package musicstore.auth

import grails.plugin.springcache.annotations.Cacheable
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import auth.User
import org.apache.shiro.SecurityUtils

class ProfileController {

	static defaultAction = "display"

	@Cacheable(cache = "profileCache", cacheResolver = "profileCacheResolver")
	def display = {
		def user = currentUser
		if (user) {
			[userInstance: user]
		} else {
			response.sendError SC_UNAUTHORIZED
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

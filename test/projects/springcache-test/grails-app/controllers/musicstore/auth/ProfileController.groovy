package musicstore.auth

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import grails.plugin.springcache.annotations.Cacheable

class ProfileController {

	static defaultAction = "display"

	def authenticateService

	@Cacheable(cache = "profileCache", cacheResolver = "profileCacheResolver")
    def display = {
		def currentUser = authenticateService.userDomain()
		if (currentUser) {
		[userInstance: currentUser]
		} else {
			response.sendError SC_UNAUTHORIZED
		}
	}
}

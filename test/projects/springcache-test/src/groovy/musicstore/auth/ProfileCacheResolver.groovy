package musicstore.auth

import grails.plugin.springcache.DefaultCacheResolver

class ProfileCacheResolver extends DefaultCacheResolver {

	def authenticateService

	@Override
	String resolveCacheName(String baseName) {
		def currentUser = authenticateService.userDomain()
		if (currentUser) {
			"$baseName-$currentUser.username"
		} else {
			super.resolveCacheName(baseName)
		}
	}


}

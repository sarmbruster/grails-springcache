package pirates

import grails.plugin.springcache.CacheResolver

class PiraticalContextCacheResolver implements CacheResolver {

	def piracyService

	String resolveCacheName(String baseName) {
		baseName + "-" + piracyService.currentContext
	}

}

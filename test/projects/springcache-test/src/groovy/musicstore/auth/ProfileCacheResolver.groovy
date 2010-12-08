package musicstore.auth

import grails.plugin.springcache.DefaultCacheResolver
import org.apache.shiro.SecurityUtils

class ProfileCacheResolver extends DefaultCacheResolver {

	@Override
	String resolveCacheName(String baseName) {
		def currentUserName = SecurityUtils.subject?.principal
		if (currentUserName) {
			"$baseName-$currentUserName"
		} else {
			super.resolveCacheName(baseName)
		}
	}


}

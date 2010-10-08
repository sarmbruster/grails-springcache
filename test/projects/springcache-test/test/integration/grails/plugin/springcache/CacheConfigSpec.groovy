package grails.plugin.springcache

import grails.plugin.spock.IntegrationSpec
import net.sf.ehcache.store.MemoryStoreEvictionPolicy

class CacheConfigSpec extends IntegrationSpec {

	def springcacheCacheManager

	def "caches can be configured in app config"() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("configuredCache")

		then: "The configuration should be correct"
		cache.cacheConfiguration.timeToLiveSeconds == 86400L
	}

	def "cache defaults can be configured in app config"() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("configuredCache")

		then: "Default cache properties should have been applied to the cache"
		cache.cacheConfiguration.memoryStoreEvictionPolicy == MemoryStoreEvictionPolicy.LFU
	}

}
package pirates

import net.sf.ehcache.store.MemoryStoreEvictionPolicy
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class CacheConfigTests extends GroovyTestCase {

	def springcacheCacheManager

	void testCachesCanBeConfiguredInAppConfig() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("configuredCache")

		then: "The cache should exist"
		assertThat "configured cache", cache, not(nullValue())

		and: "The configuration should be correct"
		assertThat "configured ttl", cache.cacheConfiguration.timeToLiveSeconds, equalTo(86400L)
	}

	void testCacheDefaultsCanBeConfiguredInAppConfig() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("configuredCache")

		then: "The cache should exist"
		assertThat "configured cache", cache, not(nullValue())

		and: "Default properties should be correct"
		assertThat "default memory eviction policy", cache.cacheConfiguration.memoryStoreEvictionPolicy, equalTo(MemoryStoreEvictionPolicy.LFU)
	}

}
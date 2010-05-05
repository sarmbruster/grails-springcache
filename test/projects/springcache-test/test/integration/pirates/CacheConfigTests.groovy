package pirates

import net.sf.ehcache.store.MemoryStoreEvictionPolicy
import org.junit.Test
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

class CacheConfigTests {

	def springcacheCacheManager

	@Test
	void cachesCanBeConfiguredInAppConfig() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("ConfiguredCache")

		then: "The cache should exist"
		assertThat "configured cache", cache, not(nullValue())

		and: "The configuration should be correct"
		assertThat "configured ttl", cache.cacheConfiguration.timeToLiveSeconds, equalTo(86400L)
	}

	@Test
	void cacheDefaultsCanBeConfiguredInAppConfig() {
		when: "A cache that is configured in app config is retrieved from the cache manager"
		def cache = springcacheCacheManager.getEhcache("ConfiguredCache")

		then: "The cache should exist"
		assertThat "configured cache", cache, not(nullValue())

		and: "Default properties should be correct"
		assertThat "default memory eviction policy", cache.cacheConfiguration.memoryStoreEvictionPolicy, equalTo(MemoryStoreEvictionPolicy.LFU)
	}

}
package pirates

import grails.validation.ValidationException
import net.sf.ehcache.Cache
import net.sf.ehcache.store.MemoryStoreEvictionPolicy
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

class CachingTests extends GroovyTestCase {

	def piracyService
	def springcacheCacheManager

	@BeforeClass static void setUpData() {
		Pirate.build(name: "Blackbeard")
		Pirate.build(name: "Calico Jack")
		Pirate.build(name: "Black Bart")
		Ship.build(name: "Queen Anne's Revenge", crew: Pirate.findAllByName("Blackbeard"))
	}

	@After
	void destroyCaches() {
		springcacheCacheManager.removeCache("PirateCache")
		springcacheCacheManager.removeCache("ShipCache")
	}

	@AfterClass static void tearDownData() {
		Ship.list()*.delete()
		Pirate.list()*.delete()
	}

	@Test
	void cachedResultsShouldBeReturnedForSubsequentMethodCalls() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cachable method is called twice"
		def result1 = piracyService.listPirateNames()
		def result2 = piracyService.listPirateNames()

		then: "The first call primes the cache"
		assertThat "cache size", cache.statistics.objectCount, equalTo(1L)

		and: "The second call hits the cache"
		assertThat "cache hits", cache.statistics.cacheHits, equalTo(1L)

		and: "The same result is returned by both calls"
		assertThat "result from uncached call", result1, equalTo(["Black Bart", "Blackbeard", "Calico Jack"])
		assertThat "result from cached call", result2, equalTo(result1)
	}

	@Test
	void cachedResultsShouldNotBeReturnedForSubsequentCallWithDifferentArguments() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cacheable method is called twice with different arguments"
		def result1 = piracyService.findPirateNames("jack")
		def result2 = piracyService.findPirateNames("black")

		then: "The cache is not hit"
		assertThat "cache hits", cache.statistics.cacheHits, equalTo(0L)

		and: "The results are cached separately"
		assertThat "cache size", cache.statistics.objectCount, equalTo(2L)

		and: "The results are correct"
		assertThat "result of first call", result1, equalTo(["Calico Jack"])
		assertThat "result of second call", result2, equalTo(["Black Bart", "Blackbeard"])
	}

	@Test
	void theCacheCanBeFlushed() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		when: "A cacheable method is called"
		def result1 = piracyService.listPirateNames()

		and: "A flushing method is called"
		piracyService.newPirate("Anne Bonny")

		and: "The cacheable method is called again"
		def result2 = piracyService.listPirateNames()

		then: "The cache is not hit"
		assertThat "cache hits", cache.statistics.cacheHits, equalTo(0L)

		and: "The results from before and after flushing are different"
		assertThat "result of first call", result1, equalTo(["Black Bart", "Blackbeard", "Calico Jack"])
		assertThat "result of second call", result2, equalTo(["Anne Bonny", "Black Bart", "Blackbeard", "Calico Jack"])
	}

	@Test
	void theCacheIsFlushedEvenIfTheFlushingMethodFails() {
		given: "A cache exists"
		def cache = new Cache("PirateCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache)

		and: "The cache is primed"
		piracyService.listPirateNames()
		def initialCacheSize = cache.statistics.objectCount

		when: "A flushing method is called with parameters that will cause it to fail"
		shouldFail(ValidationException) {
			piracyService.newPirate("Blackbeard")
		}

		and: "The cache is still flushed"
		assertThat "initial cache size", initialCacheSize, equalTo(1L)
		assertThat "cache size after flush", cache.statistics.objectCount, equalTo(0L)
	}

	@Test
	void multipleCachesCanBeFlushedByASingleMethod() {
		given: "Multiple caches exist"
		def cache1 = new Cache("PirateCache", 100, false, true, 0, 0)
		def cache2 = new Cache("ShipCache", 100, false, true, 0, 0)
		springcacheCacheManager.addCache(cache1)
		springcacheCacheManager.addCache(cache2)

		and: "Both caches are primed"
		piracyService.listPirateNames()
		piracyService.listShipNames()
		def initialCache1Size = cache1.statistics.objectCount
		def initialCache2Size = cache2.statistics.objectCount

		when: "A method is called that should flush both caches"
		piracyService.newShip("Royal Fortune", ["Black Bart", "Walter Kennedy"])

		then: "Both caches are flushed"
		assertThat "initial size of cache 1", initialCache1Size, equalTo(1L)
		assertThat "size of cache 1 after flush", cache1.statistics.objectCount, equalTo(0L)
		assertThat "initial size of cache 2", initialCache2Size, equalTo(1L)
		assertThat "size of cache 2 after flush", cache2.statistics.objectCount, equalTo(0L)
	}

	@Test
	void cachesAreCreatedOnDemandIfTheyDoNotExist() {
		when: "A cachable method is called when no cache exists"
		piracyService.listPirateNames()

		then: "The cache is created when first used"
		def cache = springcacheCacheManager.getCache("PirateCache")
		assertThat "on-demand cache", cache, not(nullValue())
		assertThat "size of on-demand cache", cache.statistics.objectCount, equalTo(1L)
	}

	@Test
	void cachesCreatedOnDemandHaveDefaultConfigurationApplied() {
		when: "A cachable method is called when no cache exists"
		piracyService.listPirateNames()

		then: "The cache created has default properties applied"
		def cache = springcacheCacheManager.getCache("PirateCache")
		assertThat "on-demand cache", cache, not(nullValue())
		assertThat "on-demand cache memory eviction policy", cache.cacheConfiguration.memoryStoreEvictionPolicy, equalTo(MemoryStoreEvictionPolicy.LFU)
	}

}
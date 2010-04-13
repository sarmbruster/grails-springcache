/*
 * Copyright 2010 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache

import grails.test.GrailsUnitTestCase
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import net.sf.ehcache.constructs.blocking.BlockingCache
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import org.gmock.WithGMock
import org.hamcrest.Matcher
import static org.hamcrest.Matchers.*

import org.springframework.cache.ehcache.EhCacheFactoryBean
import grails.spring.BeanBuilder

@WithGMock
class SpringcacheServiceTests extends GrailsUnitTestCase {

	CacheManager manager
	SpringcacheService service

	void setUp() {
		super.setUp()

		manager = new CacheManager()
		manager.addCache "cache1"
		manager.addCache "cache2"

		mockLogging SpringcacheService, true
		service = new SpringcacheService(springcacheCacheManager: manager)
	}

	void tearDown() {
		super.tearDown()
		manager.removalAll()
	}

	/**
	 * Creates a matcher that matches a EHCache Element with the specified key and objectValue.
	 */
	static Matcher<Element> element(Serializable key, Object objectValue) {
		allOf(
				instanceOf(Element),
				hasProperty("key", equalTo(key)),
				hasProperty("objectValue", equalTo(objectValue))
		)
	}

	/**
	 * Creates a matcher that matches an Ehcache instance with the specified name and config properties.
	 */
	static Matcher<Ehcache> configuredCache(String name, Map config) {
		def propertyMatchers = config.collect { k, v ->
			hasProperty(k, equalTo(v))
		}
		allOf(
				instanceOf(Ehcache),
				hasProperty("name", equalTo(name)),
				hasProperty("cacheConfiguration", allOf(propertyMatchers))
		)
	}

	/**
	 * Creates a matcher that matches an Ehcache instance with the specified name and config properties.
	 */
	static Matcher<Ehcache> cacheNamed(String name) {
		allOf(
				instanceOf(Ehcache),
				hasProperty("name", equalTo(name))
		)
	}

	void testFlushAcceptsIndividualCacheNames() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush("cache1")

		assertEquals "cache1 size", 0, manager.getEhcache("cache1").size
		assertEquals "cache2 size", 1, manager.getEhcache("cache2").size
	}

	void testFlushAcceptsMultipleCacheNames() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush(["cache1", "cache2"])

		assertEquals "cache1 size", 0, manager.getEhcache("cache1").size
		assertEquals "cache2 size", 0, manager.getEhcache("cache2").size
	}

	void testFlushAcceptsCacheNamePatterns() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush(/cache[\d]/)

		assertEquals "cache1 size", 0, manager.getEhcache("cache1").size
		assertEquals "cache2 size", 0, manager.getEhcache("cache2").size
	}

	void testFlushIgnoresInvalidCacheNames() {
		service.flush("cacheZ")
	}

	void testExceptionsOnFlushAreHandled() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache1").disabled = true
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush(["cache1", "cache2"])

		fail "Not testing properly"
		assertEquals "cache2 size", 0, manager.getEhcache("cache2").size
	}

	void testFlushAllFlushesEverything() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flushAll()

		assertEquals "cache1 size", 0, manager.getEhcache("cache1").size
		assertEquals "cache2 size", 0, manager.getEhcache("cache2").size
	}

	void testClearStatistics() {
		["cache1", "cache2"].each { cacheName ->
			manager.getEhcache(cacheName).get("key")
			manager.getEhcache(cacheName).put(new Element("key", "value"))
			manager.getEhcache(cacheName).get("key")
		}

		service.clearStatistics()

		assertEquals "cache1 hit count", 0, manager.getEhcache("cache1").statistics.cacheHits
		assertEquals "cache1 miss count", 0, manager.getEhcache("cache1").statistics.cacheMisses
		assertEquals "cache2 hit count", 0, manager.getEhcache("cache2").statistics.cacheHits
		assertEquals "cache2 miss count", 0, manager.getEhcache("cache2").statistics.cacheMisses
	}

	void testWithCacheRetrievesValueFromCacheIfFound() {
		manager.getEhcache("cache1").put(new Element("key", "value"))

		assertEquals "value", service.doWithCache("cache1", "key") {
			fail "Closure should not have been invoked"
		}
	}

	void testWithCacheReturnsNullIfNullFoundInCache() {
		manager.getEhcache("cache1").put(new Element("key", null))

		assertNull service.doWithCache("cache1", "key") {
			fail "Closure should not have been invoked"
		}
	}

	void testWithCacheStoresValueReturnedByClosureIfNotFound() {
		assertEquals "value", service.doWithCache("cache1", "key") {
			return "value"
		}

		assertEquals "cache1 size", 1, manager.getEhcache("cache1").size
		assertEquals "cache1 value for key", "value", manager.getEhcache("cache1").get("key").objectValue
	}

	void testWithCacheStoresValueReturnedByClosureIfCacheElementExpired() {
		def element = new Element("key", "value")
		element.timeToLive = 1
		manager.getEhcache("cache1").put(element)
		while (!element.isExpired()) {
			Thread.sleep 250
		}

		assertEquals "value", service.doWithCache("cache1", "key") {
			return "value"
		}

		assertEquals "value for key", "value", manager.getEhcache("cache1").get("key").objectValue
		assertFalse "key should not be expired", manager.getEhcache("cache1").get("key").isExpired()
	}

	void testWithCacheStoresNullIfClosureReturnsNull() {
		assertNull service.doWithCache("cache1", "key") {
			return null
		}

		assertTrue "cache1 should contain key", manager.getEhcache("cache1").isKeyInCache("key")
		assertNull "value for key", manager.getEhcache("cache1").get("key").objectValue
	}

	void testWithCacheThrowsExceptionIfCacheNotFoundAndAutoCreateIsFalse() {
		service.autoCreateCaches = false

		shouldFail(NoSuchCacheException) {
			service.doWithCache("cacheA", "key") {
				fail "Closure should not have been invoked"
			}
		}
	}

	void testWithCacheCreatesNewCacheIfCacheNotFoundAndAutoCreateIsTrue() {
		def beanBuilder = new BeanBuilder()
		beanBuilder.beans {
			springcacheDefaultCache(EhCacheFactoryBean) { bean ->
				bean."abstract" = true
				cacheManager = manager
			}
		}
		service.applicationContext = beanBuilder.createApplicationContext()
		service.autoCreateCaches = true

		service.doWithCache("cacheA", "key") {
			return "value"
		}

		assertTrue "cacheA should exist", "cacheA" in manager.cacheNames
		assertEquals "value for key", "value", manager.getEhcache("cacheA").get("key").objectValue
	}

	void testWithBlockingCacheDoesNotDecorateCacheIfItIsABlockingCacheAlready() {
		def blockingCache = new BlockingCache(new Cache("blockingCache", 1, false, false, 1, 0))
		manager.addCache(blockingCache)

		assertEquals "value", service.doWithBlockingCache("blockingCache", "key") {
			return "value"
		}

		assertSame "blocking cache should not have been re-decorated", blockingCache, manager.getEhcache("blockingCache")
		assertEquals "value for key", "value", manager.getEhcache("blockingCache").get("key").objectValue
	}

	void testWithBlockingCacheDecoratesCacheBeforeUsingIfItIsNonBlocking() {
		manager.addCache("nonBlockingCache")

		assertEquals "value", service.doWithBlockingCache("nonBlockingCache", "key") {
			return "value"
		}

		assertEquals "nonBlockingCache should have been decorated with a BlockingCache", BlockingCache, manager.getEhcache("nonBlockingCache").getClass()
		assertEquals "value for key", "value", manager.getEhcache("nonBlockingCache").get("key").objectValue
	}

	void testWithBlockingCacheClearsLockIfExceptionIsThrownFromClosure() {
		def blockingCache = new BlockingCache(new Cache("blockingCache", 1, false, false, 1, 0))
		blockingCache.timeoutMillis = 10
		manager.addCache(blockingCache)

		shouldFail {
			service.doWithBlockingCache("blockingCache", "key") {
				throw new RuntimeException("thrown to test exception handling")
			}
		}

		// this will time out if service call did not clear the lock
		blockingCache.get("key")
	}

	void testWithBlockingCacheDoesNotTryToClearLockIfItNeverAcquiresIt() {
		def blockingCache = new BlockingCache(new Cache("blockingCache", 1, false, false, 1, 0))
		blockingCache.timeoutMillis = 10
		manager.addCache(blockingCache)

		// acquire lock on cache
		assertNull blockingCache.get("key")

		shouldFail(LockTimeoutException) {
			service.doWithBlockingCache("blockingCache", "key") {
				return "value"
			}
		}
	}

	void testDoWithCacheDelegatesToDoWithBlockingCacheIfItFindsABlockingCache() {
		def blockingCache = new BlockingCache(new Cache("blockingCache", 1, false, false, 1, 0))
		blockingCache.timeoutMillis = 10
		manager.addCache(blockingCache)

		shouldFail {
			service.doWithCache("blockingCache", "key") {
				throw new RuntimeException("thrown to test exception handling")
			}
		}

		// this will time out if service call did not clear the lock
		blockingCache.get("key")
	}
}

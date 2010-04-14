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

import grails.spring.BeanBuilder
import grails.test.GrailsUnitTestCase
import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import net.sf.ehcache.Status
import net.sf.ehcache.constructs.blocking.BlockingCache
import net.sf.ehcache.constructs.blocking.LockTimeoutException
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.springframework.cache.ehcache.EhCacheFactoryBean
import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.assertThat

@WithGMock
class SpringcacheServiceTests extends GrailsUnitTestCase {

	CacheManager manager
	SpringcacheService service

	@Before
	void setUp() {
		super.setUp()

		manager = new CacheManager()
		manager.addCache "cache1"
		manager.addCache "cache2"

		mockLogging SpringcacheService, true
		service = new SpringcacheService(springcacheCacheManager: manager)
	}

	@Test
	void flushAcceptsIndividualCacheNames() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush("cache1")

		assertThat "cache1 size", manager.getEhcache("cache1").size, equalTo(0)
		assertThat "cache2 size", manager.getEhcache("cache2").size, equalTo(1)
	}

	@Test
	void flushAcceptsMultipleCacheNames() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush(["cache1", "cache2"])

		assertThat "cache1 size", manager.getEhcache("cache1").size, equalTo(0)
		assertThat "cache2 size", manager.getEhcache("cache2").size, equalTo(0)
	}

	@Test
	void flushAcceptsCacheNamePatterns() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flush(/cache[\d]/)

		assertThat "cache1 size", manager.getEhcache("cache1").size, equalTo(0)
		assertThat "cache2 size", manager.getEhcache("cache2").size, equalTo(0)
	}

	@Test
	void flushIgnoresInvalidCacheNames() {
		service.flush("cacheZ")
	}

	@Test
	void exceptionsOnFlushAreHandled() {
		mock(manager.getEhcache("cache1")) {
			flush().raises(new IllegalStateException("this exception would be thrown if cache is not in $Status.STATUS_ALIVE"))
		}
		manager.getEhcache("cache2").put(new Element("key", "value"))

		play {
			service.flush(["cache1", "cache2"])
		}

		assertThat "cache2 size", manager.getEhcache("cache2").size, equalTo(0)
	}

	@Test
	void flushAllFlushesEverything() {
		manager.getEhcache("cache1").put(new Element("key", "value"))
		manager.getEhcache("cache2").put(new Element("key", "value"))

		service.flushAll()

		assertThat "cache1 size", manager.getEhcache("cache1").size, equalTo(0)
		assertThat "cache2 size", manager.getEhcache("cache2").size, equalTo(0)
	}

	@Test
	void clearStatistics() {
		["cache1", "cache2"].each { cacheName ->
			manager.getEhcache(cacheName).get("key")
			manager.getEhcache(cacheName).put(new Element("key", "value"))
			manager.getEhcache(cacheName).get("key")
		}

		service.clearStatistics()

		assertThat "cache1 hit count", manager.getEhcache("cache1").statistics.cacheHits, equalTo(0L)
		assertThat "cache1 miss count", manager.getEhcache("cache1").statistics.cacheMisses, equalTo(0L)
		assertThat "cache2 hit count", manager.getEhcache("cache2").statistics.cacheHits, equalTo(0L)
		assertThat "cache2 miss count", manager.getEhcache("cache2").statistics.cacheMisses, equalTo(0L)
	}

	@Test
	void doWithCacheRetrievesValueFromCacheIfFound() {
		manager.getEhcache("cache1").put(new Element("key", "value"))

		def result = service.doWithCache("cache1", "key") {
			fail "Closure should not have been invoked"
		}

		assertThat result, equalTo("value")
	}

	@Test
	void doWithCacheReturnsNullIfNullFoundInCache() {
		manager.getEhcache("cache1").put(new Element("key", null))

		def result = service.doWithCache("cache1", "key") {
			fail "Closure should not have been invoked"
		}

		assertThat result, nullValue()
	}

	@Test
	void doWithCacheStoresValueReturnedByClosureIfNotFound() {
		def result = service.doWithCache("cache1", "key") {
			return "value"
		}

		assertThat result, equalTo("value")
		assertThat "cache1 size", manager.getEhcache("cache1").size, equalTo(1)
		assertThat "cache1 value for key", manager.getEhcache("cache1").get("key").objectValue, equalTo("value")
	}

	@Test
	void doWithCacheStoresValueReturnedByClosureIfCacheElementExpired() {
		def element = new Element("key", "value")
		element.timeToLive = 1
		manager.getEhcache("cache1").put(element)
		while (!element.isExpired()) {
			Thread.sleep 250
		}

		def result = service.doWithCache("cache1", "key") {
			return "value"
		}

		assertThat result, equalTo("value")
		assertThat "cache1 value for key", manager.getEhcache("cache1").get("key").objectValue, equalTo("value")
		assertFalse "key should not be expired", manager.getEhcache("cache1").get("key").isExpired()
	}

	@Test
	void doWithCacheStoresNullIfClosureReturnsNull() {
		assertNull service.doWithCache("cache1", "key") {
			return null
		}

		assertTrue "cache1 should contain key", manager.getEhcache("cache1").isKeyInCache("key")
		assertThat "value for key", manager.getEhcache("cache1").get("key").objectValue, nullValue()
	}

	@Test(expected = NoSuchCacheException)
	void doWithCacheThrowsExceptionIfCacheNotFoundAndAutoCreateIsFalse() {
		service.autoCreateCaches = false

		service.doWithCache("cacheA", "key") {
			fail "Closure should not have been invoked"
		}
	}

	@Test
	void doWithCacheCreatesNewCacheIfCacheNotFoundAndAutoCreateIsTrue() {
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
		assertThat "value for key", manager.getEhcache("cacheA").get("key").objectValue, equalTo("value")
	}

	@Test
	void doWithBlockingCacheDoesNotDecorateCacheIfItIsABlockingCacheAlready() {
		def blockingCache = new BlockingCache(new Cache("blockingCache", 1, false, false, 1, 0))
		manager.addCache(blockingCache)

		service.doWithBlockingCache("blockingCache", "key") {
			return "value"
		}

		assertThat "blocking cache should not have been re-decorated", manager.getEhcache("blockingCache"), sameInstance(blockingCache)
		assertThat "value for key", manager.getEhcache("blockingCache").get("key").objectValue, equalTo("value")
	}

	@Test
	void doWithBlockingCacheDecoratesCacheBeforeUsingIfItIsNonBlocking() {
		manager.addCache("nonBlockingCache")

		assertEquals "value", service.doWithBlockingCache("nonBlockingCache", "key") {
			return "value"
		}

		assertThat "nonBlockingCache should have been decorated with a BlockingCache", manager.getEhcache("nonBlockingCache"), instanceOf(BlockingCache)
		assertThat "value for key", manager.getEhcache("nonBlockingCache").get("key").objectValue, equalTo("value")
	}

	@Test
	void doWithBlockingCacheClearsLockIfExceptionIsThrownFromClosure() {
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

	@Test
	void doWithBlockingCacheDoesNotTryToClearLockIfItNeverAcquiresIt() {
		def blockingCache = new BlockingCache(new Cache("blockingCache", 1, false, false, 1, 0))
		manager.addCache(blockingCache)

		// simulate lock held on cache by another thread
		mock(blockingCache) {
			get("key").raises(new LockTimeoutException())
		}

		play {
			shouldFail(LockTimeoutException) {
				service.doWithBlockingCache("blockingCache", "key") {
					return "value"
				}
			}
		}
	}

	@Test
	void doWithCacheDelegatesToDoWithBlockingCacheIfItFindsABlockingCache() {
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

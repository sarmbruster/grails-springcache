/*
 * Copyright 2010 Luke Daley
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
package grails.plugin.springcache.taglib

import grails.plugin.spock.GroovyPagesSpec
import net.sf.ehcache.*
import spock.lang.*

@Stepwise
class TagLibCachingSmokeSpec extends GroovyPagesSpec {

	def springcacheService
	def grailsApplication
	@Shared Ehcache tagLibCache
	@AutoCleanup("removalAll") @Shared def springcacheCacheManager

	def setupSpec() {
		tagLibCache = new Cache("tagLibCache", 100, false, true, 0, 0)
	}

	def setup() {
		// have to do this in setup as injection doesn't happen until after setupSpec
		if (!springcacheCacheManager.cacheExists(tagLibCache.name)) {
			springcacheCacheManager.addCache(tagLibCache)
		}
	}

	def "cacheable tags should be an instance of cachingtag"() {
		expect:
		testCachingTagLib.caching instanceof CachingTag
	}

	def "noncacheable tags should not be an instance of cachingtag"() {
		expect:
		!(testCachingTagLib.noncaching instanceof CachingTag)
	}

	def "invoking a cacheable tag should prime the cache"() {
		given:
		testCachingTagLib.value = 1

		when:
		template = "<testcaching:caching />"

		then:
		output == "1"

		and:
		cacheMisses == old(cacheMisses) + 1
		cacheSize == old(cacheSize) + 1
	}

	def "invoking the same tag again should hit the cache"() {
		given:
		testCachingTagLib.value = 2

		when:
		template = "<testcaching:caching />"

		then:
		output == "1"

		and:
		cacheHits == old(cacheHits) + 1
	}

	def "tag attributes affect the cache key"() {
		given:
		testCachingTagLib.value = 1

		when:
		template = '<testcaching:caching a="${a}" b="${b}" />'
		params = [a: "a", b: "b"]

		then:
		output == "1"
		
		and:
		cacheMisses == old(cacheMisses) + 1
	}

	def "using the same tag parameters again means the cache is hit"() {
		given:
		testCachingTagLib.value = 2

		when:
		template = '<testcaching:caching a="${a}" b="${b}" />'
		params = [a: "a", b: "b"]

		then:
		output == "1"

		and:
		cacheHits == old(cacheHits) + 1
	}

	def "using the same tag parameters in a different order hits the cache"() {
		when:
		template = '<testcaching:caching b="${b}" a="${a}" />'
		params = [a: "a", b: "b"]

		then:
		output == "1"

		and:
		cacheHits == old(cacheHits) + 1
	}

	def "using different tag parameters misses the cache"() {
		when:
		template = '<testcaching:caching a="${a}" b="${b}" />'
		params = [a: "x", b: "y"]

		then:
		output == "2"

		and:
		cacheMisses == old(cacheMisses) + 1
	}

	private long getCacheMisses() {
		return tagLibCache.statistics.cacheMisses
	}

	private long getCacheHits() {
		return tagLibCache.statistics.cacheHits
	}

	private long getCacheSize() {
		return tagLibCache.statistics.objectCount
	}

	private getTestCachingTagLib() {
		grailsApplication.mainContext["taglib.TestCachingTagLib"]
	}

}
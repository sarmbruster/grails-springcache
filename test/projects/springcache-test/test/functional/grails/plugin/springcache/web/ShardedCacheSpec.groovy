package grails.plugin.springcache.web

import grails.plugin.geb.GebSpec
import net.sf.ehcache.Ehcache
import musicstore.pages.*
import spock.lang.*

@Stepwise
class ShardedCacheSpec extends GebSpec {

	@Shared def springcacheCacheManager

	def setupSpec() {
		setUpUser "blackbeard", "Edward Teach"
		setUpUser "roundhouse", "Chuck Norris"
	}

	def cleanup() {
		go "/logout"
		page HomePage
	}

	def cleanupSpec() {
		tearDownUsers()
	}

	@Unroll("#username only sees their own profile")
	def "a user only sees their own profile"() {
		given: "a user is logged in"
		to LoginPage
		loginAs username

		when: "they visit the profile page"
		to ProfilePage

		then: "they see their own profile"
		title == expectedTitle

		and: "their profile is cached separately"
		Ehcache cache = springcacheCacheManager.getEhcache("profileCache-$username")
		cache != null
		cache.statistics.cacheMisses == 1

		where:
		username     | expectedTitle
		"blackbeard" | "Profile: Edward Teach"
		"roundhouse" | "Profile: Chuck Norris"
	}

	@Unroll("when re-visiting the profile page #username sees their own profile delivered from cache")
	def "when re-visiting the profile page a user sees their own profile delivered from cache"() {
		given: "a user is logged in"
		to LoginPage
		loginAs username

		when: "they visit the profile page"
		to ProfilePage

		then: "they see their own profile"
		title == expectedTitle

		and: "their profile is cached separately"
		Ehcache cache = springcacheCacheManager.getEhcache("profileCache-$username")
		cache.statistics.cacheHits == 1

		where:
		username     | expectedTitle
		"blackbeard" | "Profile: Edward Teach"
		"roundhouse" | "Profile: Chuck Norris"
	}
}

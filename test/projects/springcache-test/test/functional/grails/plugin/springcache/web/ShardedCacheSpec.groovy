package grails.plugin.springcache.web

import net.sf.ehcache.Ehcache
import musicstore.pages.*
import spock.lang.*
import musicstore.pages.ProfileEditPage

@Issue("http://jira.codehaus.org/browse/GRAILSPLUGINS-2167")
@Stepwise
class ShardedCacheSpec extends AbstractContentCachingSpec {

	def setupSpec() {
		setUpUser "blackbeard", "Edward Teach"
		setUpUser "roundhouse", "Chuck Norris"
	}

	def cleanup() {
		logout()
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
		cacheFor(username).statistics.cacheMisses == 1

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
		cacheFor(username).statistics.cacheHits == 1

		where:
		username     | expectedTitle
		"blackbeard" | "Profile: Edward Teach"
		"roundhouse" | "Profile: Chuck Norris"
	}

	def "when a user updates their profile the cache is flushed"() {
		given: "a user is logged in"
		to LoginPage
		loginAs "blackbeard"

		when: "they update their profile"
		to ProfileEditPage
		profile.name = "Edward Thatch"
		profile.find("button").click(ProfilePage)

		then: "they see their updated profile"
		at ProfilePage
		title == "Profile: Edward Thatch"

		and: "the cache was missed"
		cacheFor("blackbeard").statistics.cacheMisses == old(cacheFor("blackbeard").statistics.cacheMisses) + 1
	}

	private Ehcache cacheFor(String username) {
		return springcacheService.getOrCreateCache("profileCache-$username")
	}

}

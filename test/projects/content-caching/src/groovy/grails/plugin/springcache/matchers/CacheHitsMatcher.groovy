package grails.plugin.springcache.matchers

import net.sf.ehcache.Ehcache
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class CacheHitsMatcher extends TypeSafeMatcher<Ehcache> {

	static Matcher<Ehcache> hasCacheHits(long expectedCacheHits) {
		return new CacheHitsMatcher(expectedCacheHits)
	}

	private final long expectedCacheHits

	CacheHitsMatcher(long expectedCacheHits) {
		this.expectedCacheHits = expectedCacheHits;
	}

	boolean matchesSafely(Ehcache item) {
		return item.statistics.cacheHits == expectedCacheHits
	}

	void describeTo(Description description) {
		description.appendText("a cache that has ")
		if (expectedCacheHits == 0) {
			description.appendText("not been hit")
		} else {
			description.appendText("been hit $expectedCacheHits")
			if (expectedCacheHits == 1) {
				description.appendText(" time")
			} else {
				description.appendText(" times")
			}
		}
	}

	protected void describeMismatchSafely(Ehcache item, Description mismatchDescription) {
		mismatchDescription.appendText("$item.statistics.cacheHits cache hits")
	}
}

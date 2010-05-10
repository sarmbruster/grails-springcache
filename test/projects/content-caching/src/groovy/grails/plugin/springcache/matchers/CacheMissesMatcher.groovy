package grails.plugin.springcache.matchers

import net.sf.ehcache.Ehcache
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class CacheMissesMatcher extends TypeSafeMatcher<Ehcache> {

	static Matcher<Ehcache> hasCacheMisses(long expectedCacheMisses) {
		return new CacheMissesMatcher(expectedCacheMisses)
	}

	private final long expectedCacheMisses

	CacheMissesMatcher(long expectedCacheMisses) {
		this.expectedCacheMisses = expectedCacheMisses;
	}

	boolean matchesSafely(Ehcache item) {
		return item.statistics.cacheMisses == expectedCacheMisses
	}

	void describeTo(Description description) {
		description.appendText("a cache that has ")
		if (expectedCacheMisses == 0) {
			description.appendText("not been missed")
		} else {
			description.appendText("been missed $expectedCacheMisses")
			if (expectedCacheMisses == 1) {
				description.appendText(" time")
			} else {
				description.appendText(" times")
			}
		}
	}

	protected void describeMismatchSafely(Ehcache item, Description mismatchDescription) {
		int misses = item.statistics.cacheHits
		mismatchDescription.appendText("has ").appendValue(misses)
		if (misses == 1) {
			mismatchDescription.appendText(" cache miss")
		} else {
			mismatchDescription.appendText(" cache misses")
		}
	}
}

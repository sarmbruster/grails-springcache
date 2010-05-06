package grails.plugin.springcache.matchers

import net.sf.ehcache.Ehcache
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class CacheSizeMatcher extends TypeSafeMatcher<Ehcache> {

	static Matcher<Ehcache> isEmptyCache() {
		return new CacheSizeMatcher(0)
	}

	static Matcher<Ehcache> hasCacheSize(long expectedCacheSize) {
		return new CacheSizeMatcher(expectedCacheSize)
	}

	private final long expectedCacheSize

	CacheSizeMatcher(long expectedCacheSize) {
		this.expectedCacheSize = expectedCacheSize;
	}

	boolean matchesSafely(Ehcache item) {
		return item.statistics.objectCount == expectedCacheSize
	}

	void describeTo(Description description) {
		if (expectedCacheSize == 0) {
			description.appendText("an empty cache")
		} else {
			description.appendText("a cache that contains $expectedCacheSize")
			if (expectedCacheSize == 1) {
				description.appendText(" item")
			} else {
				description.appendText(" items")
			}
		}
	}
}

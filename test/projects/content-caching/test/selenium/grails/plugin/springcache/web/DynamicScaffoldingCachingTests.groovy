package grails.plugin.springcache.web

import musicstore.pages.ArtistListPage
import net.sf.ehcache.Ehcache
import static grails.plugin.springcache.matchers.CacheHitsMatcher.hasCacheHits
import static grails.plugin.springcache.matchers.CacheMissesMatcher.hasCacheMisses
import static org.hamcrest.MatcherAssert.assertThat

class DynamicScaffoldingCachingTests extends AbstractContentCachingTestCase {

	Ehcache artistControllerCache

	void testCacheableAnnotationAtClassLevelIsRecognised() {
		def page = ArtistListPage.open()

		page.refresh()

		assertThat artistControllerCache, hasCacheMisses(2) // Selenium HEAD + GET
		assertThat artistControllerCache, hasCacheHits(1)
	}

}

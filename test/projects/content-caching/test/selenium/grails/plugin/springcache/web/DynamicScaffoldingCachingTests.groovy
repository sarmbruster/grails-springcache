package grails.plugin.springcache.web

import musicstore.pages.ArtistListPage
import net.sf.ehcache.Ehcache
import org.junit.Test
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat

class DynamicScaffoldingCachingTests extends AbstractContentCachingTestCase {

	Ehcache artistControllerCache

	@Test
	void cacheableAnnotationAtClassLevelIsRecognised() {
		def page = ArtistListPage.open()
		assertThat "page title", page.title, equalTo("Artist List")

		page = page.refresh()
		assertThat "page title", page.title, equalTo("Artist List")

		assertThat "cache hits", artistControllerCache.statistics.cacheHits, equalTo(1L)
		assertThat "cache misses", artistControllerCache.statistics.cacheMisses, equalTo(1L)
	}

}
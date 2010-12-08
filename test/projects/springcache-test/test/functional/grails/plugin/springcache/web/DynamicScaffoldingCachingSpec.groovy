package grails.plugin.springcache.web

import musicstore.pages.ArtistListPage
import net.sf.ehcache.Ehcache
import org.codehaus.groovy.grails.commons.ApplicationHolder
import spock.lang.Shared

class DynamicScaffoldingCachingSpec extends AbstractContentCachingSpec {

	@Shared Ehcache artistControllerCache = ApplicationHolder.application.mainContext.artistControllerCache

	def "class level cacheable annotation is recognised"() {
		when:
		to ArtistListPage
		driver.navigate().refresh()

		then:
		artistControllerCache.statistics.cacheMisses == 1L
		artistControllerCache.statistics.cacheHits == 1L
	}

}

package musicstore.modules

import musicstore.Album
import grails.converters.*
import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.web.key.MimeTypeAwareKeyGenerator
import grails.plugin.springcache.annotations.CacheKeyStrategy

class LatestController {

	@Cacheable("latestControllerCache")
	@CacheKeyStrategy(MimeTypeAwareKeyGenerator)
	def albums = {
		def albums = Album.list(sort: "dateCreated", order: "desc", max: 10)
		withFormat {
			html albumInstanceList: albums
			xml { render albums as XML }
			json { render albums as JSON }
		}
	}

}
package musicstore.modules

import grails.plugin.springcache.annotations.Cacheable
import grails.plugin.springcache.web.key.MimeTypeAwareKeyGenerator
import musicstore.Album
import grails.converters.*

class LatestController {

	@Cacheable(cache = "latestControllerCache", keyGeneratorType = MimeTypeAwareKeyGenerator)
	def albums = {
		def albums = Album.list(sort: "dateCreated", order: "desc", max: 10)
		withFormat {
			html albumInstanceList: albums
			xml { render albums as XML }
			json { render albums as JSON }
		}
	}

}
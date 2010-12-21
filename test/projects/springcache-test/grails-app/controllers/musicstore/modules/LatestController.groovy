package musicstore.modules

import grails.plugin.springcache.annotations.Cacheable
import musicstore.Album
import grails.converters.*

class LatestController {

	@Cacheable(cache = "latestControllerCache", keyGenerator = "mimeTypeAwareKeyGenerator")
	def albums = {
		def albums = Album.list(sort: "dateCreated", order: "desc", max: 10)
		withFormat {
			html albumInstanceList: albums
			xml { render albums as XML }
			json { render albums as JSON }
		}
	}

}

package resources

import grails.plugin.springcache.annotations.Cacheable

class ResourcesController {

	@Cacheable("resourcesControllerCache")
	def index = { }

}

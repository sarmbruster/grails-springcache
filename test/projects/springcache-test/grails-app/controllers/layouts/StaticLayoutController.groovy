package layouts

import grails.plugin.springcache.annotations.Cacheable

@Cacheable("layoutsCache")
class StaticLayoutController {

	static layout = "pirates"

	def index = {
		render "Yarr!"
	}
}

package layouts

import grails.plugin.springcache.annotations.Cacheable

@Cacheable("layoutsCache")
class NoLayoutController {

    def index = {
		render "O HAI!"
	}
}

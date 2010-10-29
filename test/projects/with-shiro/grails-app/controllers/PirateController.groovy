import grails.plugin.springcache.annotations.Cacheable

class PirateController {

	@Cacheable("pirateCache")
    def list = {
		[pirates: ShiroUser.list()]
	}
}

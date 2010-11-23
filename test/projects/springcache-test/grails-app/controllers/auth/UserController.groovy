package auth

import grails.plugin.springcache.annotations.Cacheable

class UserController {

	static scaffold = true
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	@Cacheable("userControllerCache")
    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
    }

}

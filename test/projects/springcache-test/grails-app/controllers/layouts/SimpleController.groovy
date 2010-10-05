package layouts

import grails.plugin.springcache.annotations.Cacheable

@Cacheable("layoutsCache")
class SimpleController {
    def index = {
        render "Index page"
    }

    def hello = {
        render "Hello world!"
    }
}

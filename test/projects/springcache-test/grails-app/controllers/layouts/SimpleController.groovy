package layouts

import grails.plugin.springcache.annotations.Cacheable

@Cacheable("simpleCache")
class SimpleController {
    def index = {
        render "Index page"
    }

    def hello = {
        render "Hello world!"
    }
}

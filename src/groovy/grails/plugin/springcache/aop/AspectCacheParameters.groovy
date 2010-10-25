package grails.plugin.springcache.aop

import grails.plugin.springcache.CacheParameters

class AspectCacheParameters implements CacheParameters {

	final Object target
	final String name
	final Object[] arguments

}

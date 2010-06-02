package grails.plugin.springcache.test

import grails.plugin.springcache.annotations.*

class CachingService {

	static scope = 'prototype'
	
	def value = 0
	
	@Cacheable('cachingServiceCache')
	def addValueTo(arg) {
		value + arg
	}

	@CacheFlush(['cachingServiceCache'])
	def flush() { }
}


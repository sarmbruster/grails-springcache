/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springcache.taglib

import spock.lang.*
import grails.plugin.spock.*
import grails.plugin.springcache.test.TestCachingTagLib
import grails.plugin.springcache.taglib.CachingTag

class TagLibCachingSmokeSpec extends GroovyPagesSpec {

	def springcacheService
	def grailsApplication

	def "cacheable tags should be an instance of cachingtag"() {
		expect:
		testCachingTagLib.caching instanceof CachingTag
	}

	def "noncacheable tags should not be an instance of cachingtag"() {
		expect:
		!(testCachingTagLib.noncaching instanceof CachingTag)
	}
	
	def "cacheable tags should exhibit caching behaviour"() {
		given: 
		testCachingTagLib.value = 1
		when:
		template = '<testcaching:caching />'
		then:
		output == "1"
		
		when:
		testCachingTagLib.value = 2
		and:
		template = '<testcaching:caching />'
		then:
		output == "1"
	}

	protected getTestCachingTagLib() {
		grailsApplication.mainContext['grails.plugin.springcache.test.TestCachingTagLib']
	}
	
}
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits "global"
	log "warn"
	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://repository.codehaus.org/"
		mavenRepo "http://m2repo.spockframework.org/ext/"
	}
	dependencies {
		test "org.seleniumhq.selenium:selenium-firefox-driver:2.0a5"
		test "org.hamcrest:hamcrest-all:1.2"
		test("org.codehaus.groovy.modules.http-builder:http-builder:0.5.0") {
			excludes "groovy"
		}
	}
}
grails.plugin.location.springcache = "../../.."
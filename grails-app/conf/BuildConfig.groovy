grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
	inherits("global")
	log "warn"
	repositories {
		grailsHome()
		grailsPlugins()
		grailsCentral()
		mavenLocal()
		mavenRepo "http://m2repo.spockframework.org/ext/"
		mavenRepo "http://m2repo.spockframework.org/snapshots/"
		mavenCentral()
	}
	dependencies {
		compile("net.sf.ehcache:ehcache-web:2.0.2") {
			excludes "ehcache-core", "xml-apis" // ehcache-core is provided by Grails
		}
		test("org.hamcrest:hamcrest-all:1.2") {
			exported = false
		}
		test("org.gmock:gmock:0.8.0") {
			excludes "junit"
			exported = false
		}
		test("org.objenesis:objenesis:1.2") {
			exported = false
		}
	}
	plugins {
	}
}

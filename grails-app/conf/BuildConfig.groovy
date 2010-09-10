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
		mavenCentral()
	}
	dependencies {
		compile("net.sf.ehcache:ehcache-web:2.0.0") {
			excludes "ehcache-core" // ehcache-core is provided by Grails
		}
		test("org.gmock:gmock:0.8.0") {
			excludes "junit"
		}
		test "org.hamcrest:hamcrest-all:1.1"
		test("org.spockframework:spock-grails-support:0.5-groovy-1.7-SNAPSHOT") {
			excludes "grails-crud", "grails-gorm", "grails-test", "grails-webflow", "spring-webflow", "oscache", "hsqldb", "servlet-api", "slf4j-log4j12", "jstl", "svnkit", "junit", "ant-junit"
			exported = false
		}
		test "org.objenesis:objenesis:1.2"
	}
	plugins {
		test(":spock:0.5-groovy-1.7-SNAPSHOT") {
			excludes "spock-grails-support"
			exported = false
		}
	}
}

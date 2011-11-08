grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits("global") {
		excludes "xml-apis"
	}
	log "warn"
	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://repository.codehaus.org/"
	}
	dependencies {
		test "org.seleniumhq.selenium:selenium-firefox-driver:latest.integration"
		test("org.codehaus.groovy.modules.http-builder:http-builder:0.5.1") {
			excludes "groovy", "xml-apis", "commons-logging"
		}
		test "org.codehaus.geb:geb-spock:0.6.0"
	}
	plugins {
		compile ":bean-fields:0.5"
		test ":build-test-data:1.1.1"
		compile ":cache-headers:1.1.5"
		test ":geb:0.6.0"
		compile ":hibernate:$grailsVersion"
		compile ":rateable:0.6.2"
		compile ":shiro:1.1.1"
		test ":spock:0.6-groovy-1.7-SNAPSHOT"
		build ":tomcat:$grailsVersion"
		compile ":yui:2.7.0.1"
		compile ":resources:1.0.2"
		runtime ":jquery:1.6.1.1"
	}
	plugins {
		build ":tomcat:$grailsVersion"
		compile ":bean-fields:0.5"
		compile ":cache-headers:1.1.2"
		compile ":rateable:0.6.2"
		compile ":shiro:1.1.1"
		compile ":yui:2.7.0.1"
		runtime ":hibernate:$grailsVersion"
		test ":build-test-data:1.1.1"
		test ":geb:0.5.1"
		test ":spock:0.5-groovy-1.7"
	}
}
grails.plugin.location.springcache = "../../.."

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
    inherits("global")
    log "warn"
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
    }
    dependencies {
		test "org.seleniumhq.selenium:selenium-firefox-driver:2.0a5"
    }
}

grails.plugin.location.springcache = "../../.."

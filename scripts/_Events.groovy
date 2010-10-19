eventAllTestsStart = {
	if (grailsAppName == "springcache") {
		functionalTests << classLoader.loadClass("grails.plugin.spock.test.GrailsSpecTestType").newInstance('spock', 'functional')
	}
}
package pirates

import org.springframework.aop.framework.AopContext
import grails.plugin.springcache.annotations.*
import org.codehaus.groovy.runtime.metaclass.OwnedMetaClass
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder

class PiracyService {

	static transactional = false

	@Cacheable("pirateCache")
	List listPirateNames() {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	List getAllPirateNames() {
		listPirateNames()
	}

	@Cacheable(cache = "pirateCache")
	List findPirateNames(String name, boolean reverse = false) {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			ilike "name", "%$name%"
			order "name", (reverse ? "desc" : "asc")
		}
	}

	@Cacheable("shipCache")
	List listShipNames() {
		Ship.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@CacheFlush("pirateCache")
	void newPirate(String name) {
		new Pirate(name: name).save(failOnError: true)
	}

	@CacheFlush(["pirateCache", "shipCache"])
	void newShip(String name, List crewNames) {
		new Ship(name: name, crew: crewNames.collect {
			Pirate.findByName(it) ?: new Pirate(name: it)
		}).save(failOnError: true)
	}
}
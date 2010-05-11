package pirates

import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable

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

	@Cacheable("pirateCache")
	List findPirateNames(String name) {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			ilike "name", "%$name%"
			order "name", "asc"
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
package pirates

import grails.plugin.springcache.annotations.*

class PiracyService {

	static transactional = false

	@Cacheable("pirateCache")
	List<String> listPirateNames() {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	List<String> getAllPirateNames() {
		listPirateNames()
	}

	@Cacheable(cache = "pirateCache")
	List<String> findPirateNames(String name, boolean reverse = false) {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			ilike "name", "%$name%"
			order "name", (reverse ? "desc" : "asc")
		}
	}

	@Cacheable("shipCache")
	List<String> listShipNames() {
		Ship.withCriteria {
			projections {
				property "name"
			}
			order "name", "asc"
		}
	}

	@CacheFlush("pirateCache")
	void newPirate(String name) {
		new Pirate(name: name, context: currentContext).save(failOnError: true)
	}

	@CacheFlush(["pirateCache", "shipCache"])
	void newShip(String name, List crewNames) {
		new Ship(name: name, crew: crewNames.collect {
			Pirate.findByName(it) ?: new Pirate(name: it, context: currentContext)
		}).save(failOnError: true)
	}

	Context currentContext = Context.Historical

	@Cacheable(cache = "pirateCache", cacheResolver = "piraticalContextCacheResolver")
	List<String> listPiratesForContext() {
		Pirate.withCriteria {
			projections {
				property "name"
			}
			eq "context", currentContext
			order "name", "asc"
		}
	}
}
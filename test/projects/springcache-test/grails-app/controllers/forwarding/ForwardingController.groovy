package forwarding

import grails.plugin.springcache.annotations.Cacheable

class ForwardingController {

	@Cacheable("forwardingControllerCache")
	def cachedForwardsToCached = { forward action: "cachedAction" }

	@Cacheable("forwardingControllerCache")
	def cachedForwardsToUncached = { forward action: "uncachedAction" }

	def uncachedForwardsToCached = { forward action: "cachedAction" }

	def uncachedForwardsToUncached = { forward action: "uncachedAction" }

	@Cacheable("forwardingControllerCache")
	def cachedAction = { render contentType: "text/plain", text: System.currentTimeMillis() }

	def uncachedAction = { render contentType: "text/plain", text: System.currentTimeMillis() }

}

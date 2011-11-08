package grails.plugin.springcache.web

import net.sf.ehcache.constructs.web.*

class GrailsPageInfo extends PageInfo {

	private final Map<String, ? extends Serializable> requestAttributes

	GrailsPageInfo(int statusCode, String contentType, Collection cookies, byte[] body, boolean storeGzipped, long timeToLiveSeconds, Collection<Header<? extends Serializable>> headers, Map<String, ? extends Serializable> requestAttributes) {
		super(statusCode, contentType, cookies, body, storeGzipped, timeToLiveSeconds, headers)
		this.requestAttributes = requestAttributes
	}

	Map<String, ? extends Serializable> getRequestAttributes() {
		requestAttributes.asImmutable()
	}

}

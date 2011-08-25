package grails.plugin.springcache.web

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.springframework.web.filter.GenericFilterBean
import static grails.plugin.springcache.web.GrailsFragmentCachingFilter.X_SPRINGCACHE_CACHED

class NoOpFilter extends GenericFilterBean {
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        response.addHeader(X_SPRINGCACHE_CACHED, "disabled")
        chain.doFilter(request, response)
    }
}

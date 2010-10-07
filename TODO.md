# Bugs

* direct access to SpringcacheService breaks if springcache.disabled set in config [GRAILSPLUGINS-2497][2497]
* check caching of service methods with defaulted params
* HEAD and GET requests seem to generate different keys

# Enhancements

## General

* deprecate springcache.disabled=true in favour of springcache.enabled=false
* expose caching statistics (JMX?)
* support reloading via onChange and onConfigChange [GRAILSPLUGINS-1825][1825]

## Controller caching

* request method aware key generator (different cache key for HTTP GET/POST/HEAD, etc.)
* specify key strategy with annotation on action [GRAILSPLUGINS-2548][2548]
* specify parameters to include/exclude from key with annotation on action
* option to disable content caching filter
* configurable cacheable & cacheflush for controllers in plugins?

## Method caching

* apply annotations to services at class level

## Tag lib caching

* use annotations on taglibs
* cacheable version of g:render?

[2497]:http://jira.codehaus.org/browse/GRAILSPLUGINS-2497
[1825]:http://jira.codehaus.org/browse/GRAILSPLUGINS-1825
[2548]:http://jira.codehaus.org/browse/GRAILSPLUGINS-2548
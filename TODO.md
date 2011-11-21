# Bugs

* check caching of service methods with defaulted params

# Enhancements

## General

* convert annotation classes to Groovy so they can use null defaults
* support reloading via onChange and onConfigChange [GRAILSPLUGINS-1825][1825]

## Controller caching

* request method aware key generator (different cache key for HTTP GET/POST/HEAD, etc.)
* i18n aware key generator
* XHR aware key generator
* specify parameters to include/exclude from key with annotation on action
* option to disable content caching filter
* configurable cacheable & cacheflush for controllers in plugins?

## Method caching

* apply annotations to services at class level

## Tag lib caching

* cacheable version of g:render?

[1825]:http://jira.codehaus.org/browse/GRAILSPLUGINS-1825

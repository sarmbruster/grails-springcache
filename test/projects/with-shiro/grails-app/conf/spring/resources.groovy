import org.springframework.cache.ehcache.EhCacheFactoryBean

beans = {

	pirateCache(EhCacheFactoryBean) {
		cacheManager = ref("springcacheCacheManager")
		blocking = true
	}
	
}

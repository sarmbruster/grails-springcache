import grails.plugin.springcache.web.key.WebContentKeyGenerator
import musicstore.auth.ProfileCacheResolver
import musicstore.binding.CustomPropertyEditorRegistrar
import pirates.PiraticalContextCacheResolver

beans = {
	musicStoreEditorRegistrar(CustomPropertyEditorRegistrar)

	mimeTypeAwareKeyGenerator(WebContentKeyGenerator) {
		format = true
	}

	piraticalContextCacheResolver(PiraticalContextCacheResolver) {
		piracyService = ref("piracyService")
	}

	profileCacheResolver(ProfileCacheResolver)
}
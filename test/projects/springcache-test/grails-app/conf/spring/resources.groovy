import musicstore.auth.ProfileCacheResolver
import musicstore.binding.CustomPropertyEditorRegistrar
import pirates.PiraticalContextCacheResolver
import grails.plugin.springcache.web.key.MimeTypeAwareKeyGenerator

beans = {
	musicStoreEditorRegistrar(CustomPropertyEditorRegistrar)
	
	mimeTypeAwareKeyGenerator(MimeTypeAwareKeyGenerator)

	piraticalContextCacheResolver(PiraticalContextCacheResolver) {
		piracyService = ref("piracyService")
	}

	profileCacheResolver(ProfileCacheResolver)
}
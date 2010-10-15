import musicstore.binding.CustomPropertyEditorRegistrar
import pirates.PiraticalContextCacheResolver
import musicstore.auth.ProfileCacheResolver
import pirates.PiraticalContextCacheResolver

beans = {
	musicStoreEditorRegistrar(CustomPropertyEditorRegistrar)

	piraticalContextCacheResolver(PiraticalContextCacheResolver) {
		piracyService = ref("piracyService")
	}

	profileCacheResolver(ProfileCacheResolver) {
		authenticateService = ref("authenticateService")
	}
}
import musicstore.auth.ProfileCacheResolver
import musicstore.binding.CustomPropertyEditorRegistrar
import pirates.PiraticalContextCacheResolver

beans = {
	musicStoreEditorRegistrar(CustomPropertyEditorRegistrar)

	piraticalContextCacheResolver(PiraticalContextCacheResolver) {
		piracyService = ref("piracyService")
	}

	profileCacheResolver(ProfileCacheResolver)
}
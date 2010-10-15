import musicstore.binding.CustomPropertyEditorRegistrar
import social.PiraticalContextCacheResolver

beans = {
	musicStoreEditorRegistrar(CustomPropertyEditorRegistrar)

	piraticalContextCacheResolver(PiraticalContextCacheResolver) {
		piracyService = ref("piracyService")
	}
}
package musicstore.binding

import musicstore.Artist
import org.springframework.beans.*

class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

	void registerCustomEditors(PropertyEditorRegistry registry) {
		registry.registerCustomEditor Artist, new DomainClassLookupPropertyEditor(domainClass: Artist, property: "name")
	}

}

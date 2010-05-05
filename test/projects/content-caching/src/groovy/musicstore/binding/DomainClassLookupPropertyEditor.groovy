package musicstore.binding

import java.beans.PropertyEditorSupport
import org.apache.commons.lang.StringUtils

class DomainClassLookupPropertyEditor extends PropertyEditorSupport {

	Class domainClass
	String property

	String getAsText() {
		value."$property"
	}

	void setAsText(String text) {
		if (text) {
			value = domainClass."findBy${StringUtils.capitalize(property)}"(text) ?: domainClass.newInstance((property): text)
		} else {
			value = null
		}
	}
}

package pirates

class Pirate {

	String name
	Context context

	static constraints = {
		name blank: false, unique: true
	}

}

enum Context {
	Historical, Fictional
}
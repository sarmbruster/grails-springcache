package auth

class User {

	String username
	String passwordHash
	String name

	static hasMany = [roles: Role, permissions: String]

	static constraints = {
		username nullable: false, blank: false
	}
}

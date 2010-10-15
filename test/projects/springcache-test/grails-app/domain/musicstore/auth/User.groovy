package musicstore.auth

class User {

	static transients = ['pass']
	static hasMany = [authorities: Role]
	static belongsTo = Role

	String username
	String userRealName
	String passwd
	boolean enabled
	String email
	boolean emailShow
	String description = ''
	String pass = '[secret]'

	static constraints = {
		username blank: false, unique: true
		userRealName blank: false
		passwd blank: false
	}
}

import grails.util.GrailsUtil
import auth.User
import org.apache.shiro.crypto.hash.Sha256Hash

class BootStrap {

	def init = {servletContext ->
		if (GrailsUtil.environment == "development") {
			User.withTransaction {
				def user = new User(username: "blackbeard", passwordHash: new Sha256Hash("password").toHex(), name: "Edward Teach")
				user.addToPermissions("*:*")
				println user.dump()
				user.save(failOnError: true)
			}
		}
	}

	def destroy = {
	}
} 
import org.apache.shiro.crypto.hash.Sha1Hash
import org.apache.shiro.crypto.hash.Sha256Hash

class BootStrap {

	def init = { servletContext ->
		["blackbeard", "calicojack", "blackbart", "henrymorgan"].each {
			def user = new ShiroUser(username: it, passwordHash: new Sha256Hash("password").toHex())
			user.addToPermissions("*:*")
			user.save()
		}
	}

	def destroy = {
	}
}

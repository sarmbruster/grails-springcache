import org.apache.shiro.crypto.hash.Sha1Hash
import org.apache.shiro.crypto.hash.Sha256Hash

class BootStrap {

	def init = { servletContext ->
		[blackbeard: "Edward Teach", calicojack: "Jack Rackham", blackbart: "Bartholomew Roberts", capnmorgan: "Henry Morgan"].each {
			def user = new ShiroUser(username: it.key, passwordHash: new Sha256Hash("password").toHex(), name: it.value)
			user.addToPermissions("*:*")
			user.save()
		}
	}

	def destroy = {
	}
}

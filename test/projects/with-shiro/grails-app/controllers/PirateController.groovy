class PirateController {

    def list = {
		[pirates: ShiroUser.list()]
	}
}

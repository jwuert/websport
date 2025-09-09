class LogoutAction {
	constructor() {}
	
	isEnabled() {
		return true;
	}
	
	getLabel() {
		return "Logout";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		_data = null;
		sessionStorage.setItem("data", null);
		if (ws) {
			ws.close();
	        ws = null;
		}
		window.location.href = "../jsp/logout.jsp";
	}
}

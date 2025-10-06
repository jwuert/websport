class LogoutAllAction {
	constructor() {}
	
	isEnabled() {
		return true;
	}
	
	getLabel() {
		return "Logout (All)";
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

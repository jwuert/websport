class ValidateAction {
	constructor() {}
	
	isEnabled() {
		return _data!=null;
	}
	
	getLabel() {
		return "Validate";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		if (ws) {
			ws.send("{\"command\":\"validate\", \"data\":" + JSON.stringify(_data) + "}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

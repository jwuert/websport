class BroadcastAction {
	constructor() {}
	
	isEnabled() {
		return true;
	}
	
	getLabel() {
		return "Broadcast";
	}
	
	init() {
		var paramName = {name:"msg", label:"Message", type:"text", codeList:[], defaultValue:"Test"};
		var parameterList = [paramName];
		dialog("broadcast", "Broadcast", parameterList);
	}
	
	invoke(result) {
		if (ws) {
			ws.send("{\"command\":\"broadcast\", \"value\":\"" + result.msg + "\"}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

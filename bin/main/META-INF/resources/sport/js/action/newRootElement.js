class NewRootElementAction {
	constructor() {}
	
	isEnabled() {
		return (true);
	}
	
	getLabel() {
		return "New Document";
	}
	
	init() {
		var paramName = {name:"name", label:"Name", type:"text", codeList:[], defaultValue:"Untitled"};
		var parameterList = [paramName];
		dialog("newRootElement", "New Document", parameterList);
	}
	
	invoke(result) {
		var name = result.name;
		if (ws) {
			ws.send("{\"command\":\"newRootElement\", \"name\":" + JSON.stringify(name) + "}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

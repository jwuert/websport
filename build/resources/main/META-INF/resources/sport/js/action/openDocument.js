class OpenDocumentAction {
	constructor() {}
	
	isEnabled() {
		return true;
	}
	
	getLabel() {
		return "Open Document";
	}
	
	init() {
		var paramID = {name:"id", label:"Document", type:"select", codeList:documentMap, defaultValue:""};
		var parameterList = [paramID];
		dialog("openDocument", "Open Document", parameterList);
	}
	
	invoke(result) {
		var id = result.id;
		if (ws) {
			ws.send("{\"command\":\"openDocument\", \"id\":" + id + "}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

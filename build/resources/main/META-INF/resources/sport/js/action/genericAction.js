class GenericAction {
	constructor(actionName, requiresData, parameterList) {
		this.actionName = actionName;
		this.requiresData = requiresData;
		this.parameterList = parameterList;
	}
	
	isEnabled() {
		return _data!=null || !this.requiresData;
	}
	
	getLabel() {
		return this.actionName;
	}
	
	init() {
		// this.invoke();
		console.log(this.parameterList);
		dialog(this.actionName, this.actionName, this.parameterList);
	}
	
	invoke(result) {
		if (ws) {
			ws.send("{\"command\":\""+this.actionName+"\", " + (_data!=null ? "\"rootId\":" + _data.id + ", " : "") + "\"selectionId\":" + (_selection==undefined || _selection==null ? 0 : _selection.id) + "}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

class SaveAction {
	constructor() {}
	
	isEnabled() {
		return _dirty;
	}
	
	getLabel() {
		return "Save";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		if (ws) {
			// console.log("save data length: " + ("{\"command\":\"save\", \"data\":" + JSON.stringify(_data) + ", \"selectedId\":" + _selection.id + "}").length);
			ws.send("{\"command\":\"validate\", \"data\":" + JSON.stringify(_data) + "}");
			ws.send("{\"command\":\"save\", \"data\":" + JSON.stringify(_data) + ", \"selectedId\":" + _selection.id + "}");
			_dirty = false;
		    _history = [];
		    _future = [];
		    _next_id = -1;
			updateToolbar();
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

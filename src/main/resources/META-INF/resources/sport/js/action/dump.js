class DumpAction {
	constructor() {}
	
	isEnabled() {
		return _data!=null;
	}
	
	getLabel() {
		return "Dump";
	}
	
	init() {
		this.invoke();
	}
	
	invoke() {
		action_dump_data(_data, 0);
		if (_selection) {
			console.log("selection: " + _selection["attributes"]["name"] + ": " + _selection["type"] + " [" + _selection["id"] + "]");
		}
		console.log("History:");
		for (var i=0; i<_history.length; i++) {
			console.log(_history[i].info());
		}
		console.log("Future:");
		for (var i=0; i<_future.length; i++) {
			console.log(_future[i].info());
		}
//		console.log("Metadata:");
//		for (var type in _model) {
//			console.log("# " + type + " ------------------");
//			console.log("  child types:")
//			for (var t=0; t<_model[type].childTypes.length; t++) {
//				console.log(t + ". " + _model[type].childTypes[t]);
//			}
//			console.log("  attributes:")
//			for (var key in _model[type].attributes) {
//				var meta = _model[type]["attributes"][key];
//				console.log(meta);
//			}
//		}
		if (ws) {
			ws.send("{\"command\":\"dump\", \"data\":" + JSON.stringify(_data) + "}");
		} else {
			window.location.href = "../jsp/logout.jsp";
		}
	}
}

function action_dump_data(data, depth) {
	var indent = new Array(depth + 1).join(' ');
	var category = _model[data["type"]].category;
	console.log(indent + "# " + data.type+":"+category+" ["+data["id"]+", parent: "+data["parentId"]+"] {order="+data.order+"}");
	var attributeValues = data["attributes"];
	var attributeMeta = _model[data["type"]]["attributes"];
	for (var key in attributeMeta) {
		console.log(indent + "  " + key + ": " + attributeValues[key]);
	}
	var children = data["children"];
	for (var i=0; i<children.length; i++) {
		action_dump_data(children[i], depth+2);
	}
}

//_actionStatusMap.dump = function() {
//	return true;
//};

class DeleteElementAction {
	constructor() {}
	
	isEnabled() {
		return (_selection);
	}
	
	getLabel() {
		return "Logout";
	}
	
	init() {
		var message = "Please confirm to delete " + _selection.type + " '" + _selection.attributes.id + "'";
		var parameterList = [];
		parameterList.push({type:"message", label:message});
		if (_selection.id==_data.id) {
			parameterList.push({type:"message", label:"Warning: You are going to delete the whole " + _selection.type + "!"});
		}
		dialog("deleteElement", "Delete Element", parameterList);
	}
	
	invoke(result) {
		if (_selection.id==_data.id) {
			// delete whole project:
			if (ws) {
				ws.send("{\"command\":\"deleteElement\", \"id\":" + window["_data"].id + "}");
				_dirty = false;
				_history = [];
				_future = [];
				_next_id = -1;
			    _data = null;
			    _selection = null;
				updateToolbar();
				document.getElementById("navigation").innerHTML = "";
				document.getElementById("content").innerHTML = "";
			} else {
				window.location.href = "../jsp/logout.jsp";
			}
		} else {
			// delete single subtree:
			var parentElement = getElement(_data, _selection.parentId);
			var operation = new RemoveChildOperation(_selection);
			operation.execute();
			_history.push(operation);
			
			_dirty = true;
			createNavigation(_data);
			selectElement(parentElement.id, "Attributes");
		}
	}
}

function deleteTableEntry(id) {
	var element = getElement(_data, id);
	var parentElementId = element.parentId;
	var operation = new RemoveChildOperation(element);
	operation.execute();
	_history.push(operation);

	_dirty = true;
	createNavigation(_data);
	selectElement(parentElementId);
}
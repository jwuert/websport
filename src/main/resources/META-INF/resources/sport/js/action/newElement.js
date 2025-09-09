class NewElementAction {
	constructor() {}
	
	isEnabled() {
		return (_selection);
	}
	
	getLabel() {
		return "New Element";
	}
	
	init() {
		var sample = _model[_selection.type];
		var typeMap = {};
		for (var i=0; i<sample.childTypes.length; i++) {
			var tp = sample.childTypes[i];
			typeMap[tp] = tp;
		}
		var paramSelectType = {name:"type", label:"Type", type:"select", codeList:typeMap, defaultValue:"Project"};
		var parameterList = [paramSelectType];
		dialog("newElement", "New Element", parameterList);
	}
	
	invoke(result) {
		var type = result.type;
		_createNewElement(_selection.id, type);
		createNavigation(_data);
		selectElement((_next_id+1), "Attributes");
	}
}

function newElementTableEntry(id, type) {
	getElement(_data, id);
	_createNewElement(id, type);
	createNavigation(_data);
	selectElement(_selection.id);
}

function _createNewElement(id, type) {
	var attributes = {};
	attributes.id = "untitled";
	for (var key in _model[type].attributes) {
		var defValue = _model[type].attributes[key]["default"];
		if (defValue && defValue!="") {
			attributes[key] = defValue;
		}
	}
	var order = 1+maxOrderOfCategory(_selection, _model[type].category);
	var newElement = { id: _next_id, parentId: _next_id, type: type, order: order, attributes: attributes, children: [] }
	var operation = new AddChildOperation(_selection, newElement);
	operation.execute();
	_history.push(operation);
	_next_id = _next_id-1;
	_dirty = true;
	// prepare expanded nodes in navigation:
	_prepareExpand(newElement);
}

function _prepareExpand(element) {
	_expandeNode[element.id] = true;
	if (element.id!=element.parentId) {
		var parentElement = getElement(_data, element.parentId);
		_prepareExpand(parentElement);
		if (parentElement.id==parentElement.parentId) {
			// top level: add folder
			_expandeNode[element.type] = true;
		}
	}
}

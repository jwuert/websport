/*
 * model
 * - type:
 * { 'Text' : { attributes: {}, childTypes: [], category: 'Text', isCategory: false } }
 * - category:
 * { 'Field' : { attributes: {}, isCategory: true } }
 * 
 */
/*
 * data
 * 
 * element.id			: 1000
 * element.parentId		: 1000
 * element.type			: "Setting"
 * element.category		: "Setting"
 * element.order		: 7
 * element.children		: [@element]
 * element.attributes	: { id: "OPN", ...}
 */

function createCopy(original) {
	var newElement = null;
	if (original) {
		var order = 0;
		var attributes = {};
		for (var key in original.attributes) {
			attributes[key] = original.attributes[key];
		}
		newElement = { id: original.id, parentId: original.parentId, type: original.type, order: original.order, attributes: attributes, children: [] }
		for (var i=0; i<original.children.length; i++) {
			var childCopy = createCopy(original.children[i]);
			newElement.children.push(childCopy);
		}
	}
	return newElement;
}

function addNewIds(selection, pId) {
	if (selection) {
		selection.id = _next_id;
		selection.parentId = (pId ? pId : selection.id);
		_next_id = _next_id-1;
		for (var i=0; i<selection.children.length; i++) {
			addNewIds(selection.children[i], selection.id);
		}
	}
}

function lookupElement(data, type) {
	return lookupElementDepth(data, type, -1);
}

function lookupElementDepth(data, type, depth) {
	var list = [];
	if (data.type===type) {
		list.push(data);
	}
	if (depth != 0) {
		for (var i=0; i<data.children.length; i++) {
			Array.prototype.push.apply(list, lookupElementDepth(data.children[i], type, depth-1));
		}
	}
	return list;
}

function getElement(data,id) {
	if (data["id"]===id) {
		return data;
	} else {
		var children = data["children"];
		for (var i=0; i<children.length; i++) {
			var attributes = getElement(children[i],id);
			if (attributes) {
				return attributes;
			}
		}
		return null;
	}
}

function performSetAttributeValue(element, attributeName, value) {
	if (element.attributes[attributeName]!=value) {
		var operation = new SetAttributeValueOperation(element, attributeName, value);
		operation.execute();
		_history.push(operation);
		_future = [];
		_dirty = true;
		updateToolbar();
		createNavigation(_data);
		updateContentStatus();
	}
}

function performUndo() {
	var operation = _history.pop();
	_future.push(operation);
	operation.undo();
	if (_selection) {
		selectElement(_selection.id);
		updateContentStatus();
	}
	_dirty = true;
	updateToolbar();
	createNavigation(_data);
}

function performRedo() {
	var operation = _future.pop();
	_history.push(operation);
	operation.execute();
	if (_selection) {
		selectElement(_selection.id);
		updateContentStatus();
	}
	_dirty = true;
	updateToolbar();
	createNavigation(_data);
}


// internal functions:

function setAttributeValue(element, attributeName, value) {
	var attribute = element["attributes"];
	attribute[attributeName] = value;
}

function setAttributeValueRecursive(data,id,key,value) {
	if (data["id"]===id) {
		var attr = data["attributes"];
		attr[key] = value;
		return true;
	} else {
		var children = data["children"];
		for (var i=0; i<children.length; i++) {
			if (setAttributeValueRecursive(children[i],id,key,value)) {
				return true;
			}
		}
		return null;
	}
}

function addParents(data, parentId) {
	data.parentId = parentId;
	var children = data["children"];
	for (var i=0; i<children.length; i++) {
		addParents(children[i],data.id);
	}
}

function replaceId(data, idMap) {
	if (Object.keys(idMap).length===0) {
		return;
	}
	if (data["id"] in idMap) {
		var keyId = data["id"];
		data.id = idMap[keyId];
		// console.log("replaced: "+keyId+" by "+data.id);
		delete(idMap[keyId])
		if (Object.keys(idMap).length===0) {
			return;
		}
	} 
	var children = data["children"];
	for (var i=0; i<children.length; i++) {
		replaceId(children[i], idMap);
	}
}

function countChildrenOfCategory(element, category) {
	var count = 0;
	for (var i=0; i<element.children.length; i++) {
		if (_model[element.children[i].type].category==category) {
			count++;
		}
	}
	return count;
}

function maxOrderOfCategory(element, category) {
	var max = -1;
	for (var i=0; i<element.children.length; i++) {
		if (_model[element.children[i].type].category==category) {
			max = Math.max(max, element.children[i].order);
		}
	}
	return max;
}
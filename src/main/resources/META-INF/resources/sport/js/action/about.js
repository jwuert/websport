class AboutAction {
	constructor() {}
	
	isEnabled() {
		return true;
	}
	
	getLabel() {
		return "About";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		var message = "2025 by J. Würthner";
		var parameterList = [{type:"message", label:message}];
		if (_data) {
			var msgs = fullValidation(_data);
			for (var i=0; i<msgs.length; i++) {
				var msg = msgs[i];
				parameterList.push({type:"message", label:msg});
			}
			var diffList = createDelta(_data, _orig);
			if (diffList.length>0) {
				parameterList.push({type:"message", label:"---"});
			}
			for (var i=0; i<diffList.length; i++) {
				parameterList.push({type:"message", label:diffList[i].info()});
			}
		}

		parameterList.push({type:"message", label:result});
		dialog("about", "About", parameterList);
	}
}

function createDelta(tree1, tree2) {
	var diffListAttributes = compareAttributes(tree1, tree2);
	var diffListChildren = compareChildren(tree1, tree2);
	var diffList = diffListAttributes.concat(diffListChildren);
	return diffList;
}

function compareAttributes(tree1, tree2) {
	var diffList = [];
	for (var key in tree1.attributes) {
		var value1 = tree1.attributes[key];
		var value2 = tree2.attributes[key];
		if (value1 != value2) {
			var diff = new Difference("Attribute", tree1.id, tree1.attributes.id, tree1.type, value1, value2);
			diffList.push(diff);
		}
	}
	return diffList;
}

function compareChildren(tree1, tree2) {
	var diffList = [];
	var commonChildrenList = []; // [ [a, b], [c, d] ... ]
	// tree 1
	for (var i1=0; i1<tree1.children.length; i1++) {
		for (var i2=0; i2<tree2.children.length; i2++) {
			var found = false;
			if (tree1.children[i1].id==tree2.children[i2].id) {
				commonChildrenList.push( [ tree1.children[i1], tree2.children[i2] ] );
				found = true;
				break;
			}
		}
		if (!found) {
			var diff = new Difference("MissingIn2", tree1.id, tree1.attributes.id, tree1.type, tree1.children[i1].attributes.id, "-");
			diffList.push(diff);
		}
	}
	// tree 2
	for (var i2=0; i2<tree2.children.length; i2++) {
		for (var i1=0; i1<tree1.children.length; i1++) {
			var found = false;
			if (tree2.children[i2].id==tree1.children[i1].id) {
				commonChildrenList.push( [ tree2.children[i2], tree1.children[i1] ] );
				found = true;
				break;
			}
		}
		if (!found) {
			var diff = new Difference("MissingIn1", tree2.id, tree2.attributes.id, tree2.type, "-", tree2.children[i2].attributes.id);
			diffList.push(diff);
		}
	}
	//
	for (var i=0; i<commonChildrenList.length; i++) {
		var childDiffList = createDelta(commonChildrenList[i][0], commonChildrenList[i][1]);
		diffList = diffList.concat(childDiffList);
	}
	return diffList;
}

class Difference {
	constructor(type, tid, id, elementType, value1, value2) {
		this.type = type;
		this.tid = tid;
		this.id = id;
		this.elementType = elementType;
		this.value1 = value1;
		this.value2 = value2;
		console.log(this.info())
	}
	info() {
		return this.type + " " + this.id + " " + this.elementType + " " + this.value1 + " " + this.value2;
	}
}

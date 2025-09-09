class CollapseNavigationAction {
	constructor() {}
	
	isEnabled() {
		return _selection!=null || _folderSelection!=null;
	}
	
	getLabel() {
		return "CollapseNavigation";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		if (Object.keys(_expandeNode).length==0) {
			if (_folderSelection!=null) {
				_expandeNode[_folderSelection] = true;
			} else if (_selection==_data) {
				var cTypes = [... new Set(_selection.children.map(a => a.type))];
				for (var i=0; i<cTypes.length; i++) {
					_expandeNode[cTypes[i]] = true;
				}
			}
			if (_selection==null) {
				_selection = _data;
			}
			doExpandNode(_selection);
		} else {
			_expandeNode = {};
		}
		createNavigation(_data);
	}
}

function doExpandNode(element) {
	_expandeNode[element.id] = true;
	for (var i=0; i<element.children.length; i++) {
		doExpandNode(element.children[i]);
	}
}

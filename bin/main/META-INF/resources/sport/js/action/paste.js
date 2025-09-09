class PasteAction {
	constructor() {}
	
	isEnabled() {
		return (_clipboard);
	}
	
	getLabel() {
		return "Paste";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		if (_selection) {
			console.log("PASTE:");
			
			var cp = createCopy(_clipboard);
			addNewIds(cp);
			console.log(cp);
			var operation = new AddChildOperation(_selection, cp);
			operation.execute();
			_history.push(operation);
			_dirty = true;
			// prepare expanded nodes in navigation:
			// _prepareExpand(cp);
			
			createNavigation(_data);
			selectElement(_selection.id);
		}
	}
}


class CutAction {
	constructor() {}
	
	isEnabled() {
		return (_selection);
	}
	
	getLabel() {
		return "Cut";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		_clipboard = createCopy(_selection);

		// delete single subtree:
		var parentElement = getElement(_data, _selection.parentId);
		var operation = new RemoveChildOperation(_selection);
		operation.execute();
		_history.push(operation);
		
		_dirty = true;
		createNavigation(_data);
		selectElement(parentElement.id, "Attributes");
			
		updateToolbar();
	}
}

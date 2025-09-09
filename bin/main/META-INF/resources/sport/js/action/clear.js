class ClearAction {
	constructor() {}
	
	isEnabled() {
		return (_selection);
	}
	
	getLabel() {
		return "Clear";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		_clipboard = null;
		_selection = null;
		updateToolbar();
		createNavigation(_data);
		selectElement();
	}
}

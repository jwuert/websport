class CopyAction {
	constructor() {}
	
	isEnabled() {
		return (_selection);
	}
	
	getLabel() {
		return "Copy";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		_clipboard = createCopy(_selection);
		updateToolbar();
	}
}

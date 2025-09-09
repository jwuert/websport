class UndoAction {
	constructor() {}
	
	isEnabled() {
		return _history.length>0;
	}
	
	getLabel() {
		return "Undo";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		performUndo();
		if (document.getElementById("info").style.display=='inline') {
			_actionMap.journal.invoke();
		}
	}
}

class RedoAction {
	constructor() {}
	
	isEnabled() {
		return _future.length>0;
	}
	
	getLabel() {
		return "Redo";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		performRedo();
		if (document.getElementById("info").style.display=='inline') {
			_actionMap.journal.invoke();
		}
	}
}

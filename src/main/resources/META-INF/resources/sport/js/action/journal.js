class JournalAction {
	constructor() {}
	
	isEnabled() {
		return (_data!=null);
	}
	
	getLabel() {
		return "Journal";
	}
	
	init() {
		this.invoke();
	}
	
	invoke(result) {
		var parameterList = [];
		
		if (_history.length>0) {
			for (var i=Math.max(0,_history.length-3); i<_history.length; i++) {
				var message = "["+ (i+1) +"] " + _history[i].shortInfo();
				parameterList.push( {type:"message", label:message} );
			}
		}
		
		parameterList.push( {type:"message", label:"     ---"} )
		
		var counter = _history.length+1;
		if (_future.length>0) {
			for (var i=Math.min(_future.length-1,3); i>=0; i--) {
				var message = "[" + (counter++) + "] " + _future[i].shortInfo();
				parameterList.push( {type:"message", label:message} );
			}
		}
		
		parameterList.push( {type:"message", label:"   "} )
		parameterList.push( {type:"message", label:"selection: " + (_selection ? _selection.attributes.id + " [" + _selection.id + "]" : "-") } )
		
		dialog("journal", "Journal", parameterList, "font-size:small");
	}
}

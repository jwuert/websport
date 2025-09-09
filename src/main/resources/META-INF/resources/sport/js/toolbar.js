

function makeToolbar() {
	toolbar = "";
	for (var id in _actionMap) {
		var tool = _actionMap[id];
		var tooltip = (tool ? tool.getLabel() : "");
		toolbar += "<a id=\"action"+id+"\" class='nav-item nav-link' data-toggle='tooltip' title='" + tooltip + "' href='javascript:callAction(\""+id+"\")' ></a>&nbsp;&nbsp;";
	}
    document.getElementById("toolbar").innerHTML=toolbar;
    updateToolbar();
}

function updateToolbar() {
	for (var id in _actionMap) {
		var tool = _actionMap[id];
		var toolElement = (tool && document.getElementById("action"+id));
		var status = (tool && tool.isEnabled());
		var brightness = (status?100:40);
		if (toolElement) {
			toolElement.style=getStyle(id,brightness);
		}
	}
}

function getStyle(id,inv) {
	return "background-image: url(../sport/img/toolbar/"+id+".png);"+
		"padding:15px;"+
		"background-position:right;"+
		"background-repeat:no-repeat;"+
		"background-size:contain;"+
		"filter:invert("+inv+"%);";
}

function callAction(id) {
	console.log("call action: " + id);
	var tool = _actionMap[id];
	if (tool.isEnabled()) {
		tool.init();
	}
}

function initializeToolBar(actionList) {
	for (var i=0; i<actionList.length; i++) {
		// console.log(i + ", " + actionList[i])
		switch (actionList[i].id) {
			case "about": _actionMap.about= new AboutAction(); break;
			case "broadcast": _actionMap.broadcast = new BroadcastAction(); break;
			case "collapseNavigation": _actionMap.collapseNavigation = new CollapseNavigationAction(); break;
			case "deleteElement": _actionMap.deleteElement = new DeleteElementAction(); break;
			case "dump": _actionMap.dump = new DumpAction(); break;
			case "journal": _actionMap.journal= new JournalAction(); break;
			case "logout": _actionMap.logout = new LogoutAction(); break;
			case "newRootElement": _actionMap.newRootElement = new NewRootElementAction(); break;
			case "newElement": _actionMap.newElement= new NewElementAction(); break;
			case "openDocument": _actionMap.openDocument = new OpenDocumentAction(); break;
			case "save": _actionMap.save = new SaveAction(); break;
			case "undo": _actionMap.undo = new UndoAction(); break;
			case "redo": _actionMap.redo = new RedoAction(); break;
			case "cut": _actionMap.cut = new CutAction(); break;
			case "copy": _actionMap.copy = new CopyAction(); break;
			case "paste": _actionMap.paste = new PasteAction(); break;
			case "clear": _actionMap.clear = new ClearAction(); break;
			case "validate": _actionMap.validate = new ValidateAction(); break;
			case "compare": _actionMap.compare = new CompareAction(); break;
			case "separator": _actionMap["sep"+i] = null; break;
			default:
				var actionName = actionList[i].id;
				_actionMap[actionName] = new GenericAction(actionName, actionList[i].requiresData, actionList[i].parameterList);
				break;
		}
	}
	updateToolbar();
}
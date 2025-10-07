String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};

function handleMessage(ws, data) {
	jo = JSON.parse(data);
	var command = jo["command"];
	var editPermission = jo["editPermission"];
	console.log("command: " + command);
	
	if (command==="setData") {
	    // this makes sure that only the user's own changes can cause an update, in order to avoid overwriting changes made in the browser
	    if (jo["senderId"] === _userId) {
            _data = jo["data"];
            _orig = JSON.parse(JSON.stringify(_data));
            addParents(_data, _data["id"]);
            createNavigation(_data);
            selectElement(_data["id"], "Attributes");
		}
	} else if (command==="setUserId") {
        _userId = jo["userId"];
        //_maintenance = jo["maintenance"]
        console.log("websport: userId = " + _userId);
        //console.log("websport: maintenance: " + _maintenance);
        //console.log(_maintenance);
        //if (_maintenance!="" && _maintenance!=_userId) {
        //    var header = "Wartungsarbeiten";
        //    var message = "Das System wird gerade von " + _maintenance + " bearbeitet.";
        //    var parameterList = [];
        //    parameterList.push({type:"message", label:message});
        //    dialog("Info", header, parameterList);
        //    ws.close();
        //}
	} else if (command==="setModel") {
		_model = jo["data"];
		_appName = jo["appName"];
		document.getElementById("appName").innerHTML = _appName;
		var newSession = jo["newSession"]
		console.log("New Session: " + newSession);
		if (newSession) { _data = null; }
// 	} else if (command==="replaceId") {
		// IDs need to be replaced (not in the data, but) in history and future!
//		clearContent();
//		var list = jo["data"];
//		var idMap = {};
//		for (var i=0; i<list.length; i++) {
//			var map = list[i];
//			idMap[map.preId] = map.id;
//		}
//		replaceId(_data, idMap);
//		createNavigation(_data);
//		if (jo.selectedId!=0) {
//			selectElement(jo.selectedId, "Attributes");
//		}
	} else if (command==="setDocumentList") {
		var documentReferenceMap = jo["data"];
		console.log(documentReferenceMap);
		for (var name in documentReferenceMap) {
			var id = documentReferenceMap[name];
			documentMap[id] = name;
			console.log("provide document: " + id + ", name: " + name);
		}
	} else if (command==="setActionList") {
		var actionList = jo["actionList"];
		initializeToolBar(actionList);
	} else if (command==="validationResult") {
		var parameterList = [];
		var resultList = jo["data"];
		for (var i=0; i<resultList.length; i++) {
			var res = resultList[i];
			var message = res.type + ": " + res.elementType
				+ " '<a href=\"javascript:document.getElementById('info').style.display='none';selectElement(" + res.elementTId + ",'Attributes');\">" + res.elementId + "</a>'"
				+ (res.attributeName ? ", field '"+res.attributeLabel+"'" : "") + " " + res.validationMessage;
			parameterList.push({type:"message", label:message});
		}
		if (resultList.length==0) {
			parameterList.push({type:"message", label:"Data successfully validated!"});
		}
		dialog(null, "Validation", parameterList, "font-size:small");
	} else if (command==="compareResult") {
		var delta = jo["delta"];
		showComparison(delta);
	} else if (command==="action") {
		var actionName = jo["action"];
		var message = jo["message"];
		var parameterList = [];
		parameterList.push({type:"message", label:message});
		dialog("action", actionName, parameterList);
	} else if (command==="setChecks") {
//		var checkList = jo["data"];
//		for (var key in checkList) {
//			console.log(key + ", " + checkList[key]);
//			
//		}
	} else if (command==="info") {
		var header = jo["header"];
		var message = jo["message"];
		var parameterList = [];
		parameterList.push({type:"message", label:message});
		dialog("action", header, parameterList);
	}
}

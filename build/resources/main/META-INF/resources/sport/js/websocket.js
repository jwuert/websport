var ws;
var showJournal = false;
var backupImage = null;

function selectIt(id) {
	if (ws) {
		ws.send("{\"command\":\"selectElement\", \"id\":"+id+"}");
	}
}

function selectDocument() {
	var id = document.getElementById("documentSelector").value;
	if (ws) {
		ws.send("{\"command\":\"selectDocument\", \"id\":"+id+"}");
	}
}

function requestConfirm(value) {
	if (ws) {
		ws.send("{\"command\":\"confirm\", \"value\":"+value+"}");
		document.getElementById("info").style.display='none';
	}
}

function requestFunctionCall(args) {
	if (ws) {
		if (args) {
			var arg = args.split(",");
			var result = "{";
			for (var i=0; i<arg.length; i++) {
				if (i>0) {result += ",";}
				var value = document.getElementById(arg[i]).value;
				result += "\""+arg[i]+"\":\""+value+"\"";
			}
			result += "}";
			ws.send("{\"command\":\"functionCall\", \"value\":"+result+"}");
		}
		document.getElementById("info").style.display='none';
	}
}

function sendValue(id, attribute, value) {
	if (ws) {
		value = JSON.stringify(value);
		ws.send("{\"command\":\"setValue\", \"id\":"+id+", \"attribute\":\""+attribute+"\", \"value\":"+value+"}");
	}
}

function sendArrayValue(id, attribute, index, value, arrayString) {
	if (ws) {
		// console.log("* " + attribute + ", " + value + ", " + arrayString);
		ws.send("{\"command\":\"setArrayValue\", \"id\":"+id+", \"attribute\":\""+attribute+"\", \"index\":"+index+", \"value\":\""+value+"\",\"array\":\""+arrayString+"\"}");
	}
}

function addEntry(id, attribute) {
	if (ws) {
    	ws.send("{\"command\":\"addEntry\", \"id\":"+id+", \"attribute\":\""+attribute+"\"}");
	}
}

function removeEntry(id, attribute) {
	if (ws) {
    	ws.send("{\"command\":\"removeEntry\", \"id\":"+id+", \"attribute\":\""+attribute+"\"}");
	}
}

function invokeAction(id) {
	if (ws) {
		ws.send("{\"command\":\"invokeAction\", \"id\":\""+id+"\"}");
	}
}

function requestElement(parentId, id) {
	if (ws) {
		ws.send("{\"command\":\"requestElement\", \"parentId\":"+parentId+", \"id\":"+id+"}");
	}
}

function log(text) {
   // console.log( (new Date).getTime() + ": " + (!Object.isUndefined(text) && text !== null ? text.escapeHTML() : "null") );
}

function initWS() {
	console.log("Running project: " + getProjectId());
	var wsUri = "ws://" + window.location.hostname + ":" + window.location.port + "/" + getProjectId() + "/socket";
	log("connecting to: " + wsUri);
	ws = new WebSocket(wsUri);
	// ws = new WebSocket("ws://localhost:8887");
    ws.onopen = function() {
        log("[WebSocket#onopen]\n");
//        $("disconnect").style.display="block";
//        $("connect").style.display="none";
//        $("toolbar").style.display="block";
    }
    ws.onmessage = function(e) {
    	jo = JSON.parse(e.data);
        log("[WebSocket#onmessage] Message: '" + jo.command + "'\n");
        handleMessage(ws, e.data);
    }
    ws.onclose = function() {
        log("[WebSocket#onclose]\n");
        ws = null;
        document.getElementById("content").innerHTML="";
        // $("toolbarHeader").innerHTML="";
        // $("table").innerHTML="";
        // $("footer").innerHTML="";
    }
    if (ws) {
    	setTimeout(function() {
    		log("ping");
    		// ws.send("{\"command\":\"ping\"}");
    		// refactor:
    		makeToolbar();
    		if (_data!=null) {
	    		updateToolbar();
	    		createNavigation(_data);
    		}
    		// updateContentStatus();
    		// openDocument("test");
    	}, 500);
    	// $("header").innerHTML="<br/>Speedy";
    }
}

function getProjectId() {
	return window.location.pathname.split("/")[1];
}


/*
 * parameterList = []
 * parameter.type 			: one of text, select, message
 * parameter.label 			: "the label"
 * parameter.name 			: "the name"
 * parameter.defaultValue 	: "the value"
 * parameter.codeList		: {value:label, value:label, ...}
 */

function dialog(actionId, header, parameterList, attr) {
	if (parameterList.length > 0) {
		var info = "";
		info += "<div class='modal-content'>";
		info += " <div class='modal-header'>";
		info += "  <h4 class='modal-title'>" + header + "</h4>";
		info += " </div>";
		info += " <div class='modal-body '>";
		var provideCancel = false;
		for (var i=0; i<parameterList.length; i++) {
			var parameter = parameterList[i];
			if (parameter.type==="text") {
				provideCancel = true;
				// input
				info += "<div class='form-group'>";
				info += "<label for='"+parameter.name+"'>"+parameter.label+"</label>";
				info += "<input type='text' class='form-control' name='"+parameter.name+"' id='"+parameter.name+"' value='"+parameter.defaultValue+"'></input>";
				info += "</div>";
			} else if (parameter.type==="select") {
				provideCancel = true;
				// select
				info += "<div class='form-group'>";
				info += "<label for='"+parameter.name+"'>"+parameter.label+"</label>";
				info += "<select class='form-control' name='"+parameter.name+"' id='"+parameter.name+"'>";
				for (var value in parameter.codeList) {
					var label = parameter.codeList[value];
					var selected = (parameter.defaultValue==value ? "selected='selected'":"");
					info += "<option value='" + value + "' " + selected + ">"+label+"</option>";
				}
				info += "</select>";
				info += "</div>";
			} else if (parameter.name==="file") { // "name" is correct!
				info += "<div class='form-group'>";
				info += "<label for='"+parameter.name+"'>"+parameter.label+"</label>";
				info += "<p></p>";
				info += "<input id=\"files\" name=\"files[]\" type=\"file\" size=\"50\" accept=\"*\" ></input>";
				info += "</div>";
			} else if (parameter.type==="message") {
			
				// message
				info += "<p style='padding-top:0;padding-bottom:0;margin-top:0;margin-bottom:0;"+attr+"'>"+parameter.label+"</p>";
			};
		}
		var args = actionId;
		for (var i=0; i<parameterList.length; i++) {
			if (parameterList[0].type!="message") {
				args += ",";
				args += parameterList[i].name;
			}
		}
		
		info += " </div>";
		info += " <div class='modal-footer'>";
		info += "  <button id='okBtn' type='button' class='btn btn-default' onclick='javascript:parameterCall(\""+args+"\")\'>OK</button>";
		if (provideCancel) {
			info += "  <button type='button' class='btn btn-default' onclick=\"javascript:parameterCall()\">Cancel</button>";
		}
		info += " </div>";
		info += "</div>";
		
		document.getElementById("info").innerHTML=info;
		document.getElementById("info").style.display='inline';
		
		if (parameterList[0] && parameterList[0].type=="text") {
			// document.getElementById(parameterList[0].name).addEventListener('keyup',function(e){if (e.which == 13) this.blur();});
			// document.getElementById(parameterList[0].name).addEventListener('keypress', e => { if (e.key=='Enter') {console.log("yea"); this.blur();} } );
			document.getElementById(parameterList[0].name).addEventListener('keypress', e => { if (e.key=='Enter') { document.getElementById('okBtn').focus(); } } );
			document.getElementById(parameterList[0].name).select();
			document.getElementById(parameterList[0].name).focus();
		}
		if (document.getElementById('files')) {
			document.getElementById('files').addEventListener('change', handleFileSelect, false);
		}
	} else {
		parameterCall(actionId);
	}
}

function parameterCall(args) {
	if (args) {
		var arg = args.split(",");
		var actionId = arg[0];
		var result = {};
		if (actionId != 'import') {
			for (var i=1; i<arg.length; i++) {
				var value = document.getElementById(arg[i]).value;
				result[arg[i]] = value;
			}
		}
		if (actionId != null && actionId != "null" && actionId != 'action') {
			_actionMap[actionId].invoke(result);
		}
	}
	document.getElementById("info").style.display='none';
}

function handleFileSelect(evt) {
	document.getElementById("files").removeEventListener("change",handleFileSelect);
	var files = evt.target.files;
	var reader = new FileReader();
	reader.onload = function(theFileData) {
		var data = theFileData.target.result;
		ws.send("{\"command\":\"transfer\", \"data\":\""+data+"\"}");
		document.getElementById("info").style.display='none';
	}
	reader.readAsDataURL(files[0]);
}

/*
 * Content Area
 */
function selectElement(id, editorType) {
	renderSelectedNavItem(id);
	_folderSelection = null;
	var editPermission = true;
	var element = getElement(_data, id);
	_selection = element;
	if (editorType) {
		_editor = editorType;
	}
	var html = "";
	if (element) {
		var onclick = "setEditor(this.getAttribute(\"value\"))";
		console.log("element.type: " + element.type + ", m: " + _model[element.type]);

		var childTypes = _model[element.type].childTypes;
		var childCategories = [...new Set(childTypes.map( tp => _model[tp].category ))];
		html += "<nav>";
		html += " <div class='nav nav-tabs' id='nav-tab' role='tablist'> ";
		var activeAttr = (_editor=="Attributes" ? "active" : "");
		html += "   <a class='nav-item nav-link "+activeAttr+"' id='menuAttributes-tab' data-toggle='tab' href='#menuAttributes' role='tab' aria-controls='menuAttributes' aria-selected='true' value='Attributes' onclick='"+onclick+"'>Attributes</a>";
		for (var t=0; t<childCategories.length; t++) {
			var active = (_editor==childCategories[t] ? "active" : "");
			html += "   <a class='nav-item nav-link "+active+"' id='menu"+childCategories[t]+"-tab' data-toggle='tab' href='#menu"+childCategories[t]+"' role='tab' aria-controls='menu"+childCategories[t]+"' aria-selected='false' value='"+childCategories[t]+"' onclick='"+onclick+"'>" + childCategories[t] + "</a>";
		}
		html += " </div>";
		html += "</nav>";
		html += "<br/>";
		html += " <div class='tab-content' id='nav-tabContent'> ";
		var activeAttr = (_editor=="Attributes" ? "show active" : "");
		html += "   <div id='menuAttributes' role='tabpanel' aria-labelledby='menuAttributes-tab' class='tab-pane fade "+activeAttr+"'> ";
		// ATTRIBUTE EDITOR
		html += makeAttributeEditor(element, editPermission);
		html += "   </div> ";
		for (var t=0; t<childCategories.length; t++) {
			var active = (_editor==childCategories[t] ? "show active" : "");
			html += "   <div id='menu" + childCategories[t] + "' role='tabpanel' aria-labelledby='menu"+childCategories[t]+"-tab' class='tab-pane fade "+active+"'> ";
			// TABLE EDITOR
			html += makeTableEditor(element, editPermission, childCategories[t]);
			html += "   </div> ";
		}
		html += " </div> ";
		
	} else {
		html = "-";
	}
	document.getElementById("content").innerHTML = html;
	updateContentStatus();
	updateToolbar();
}

function setEditor(editor) {
	_editor = editor;
	updateContentStatus();
}

function makeAttributeEditor(element, editPermission) {
	var type = element["type"];
	var id = element.id;
	var attributes = element["attributes"];
	var html = "";
	html += "<div id=\"EL_"+id+"\">";
	html += "<table style='width:100%'><tr><td></td><td></td></tr>";
	var attributeNo = 0;
	var showLabel = true;
	for (var key in _model[type].attributes) {
		var meta = _model[type]["attributes"][key];
		html += "<tr id=\"p"+key+"\" class=\"tablerow\">";
		html += makeBSInputField(element, meta, key, attributes[key], editPermission, showLabel, attributeNo===0);
		html += "</tr>";
		attributeNo++;
	}
	html += "</table>";
	html += "</div>";
	return html;
}

function makeTableEditor(element, editPermission, category) {
	var id = element.id;
	var html = "";
	html += "<div id=\"EL_"+id+"\">";
	html += "<table class='tableEditor'>";
	// header
	html += "<tr>";
	html += "<th width='70px;'></th>";
	for (var key in _model[category].attributes) {
		var attr = _model[category].attributes[key];
		if (attr.type != "Display" && attr.type != "Textarea" && attr.type != "StaticMultiSelect" && attr.type != "DynamicMultiSelect" && attr.class != "ClassAttribute") {
			html += "<th>";
			html += attr.label;
			html += "</th>";
		}
	}
	html += "</tr>";
	// rows
	var children = element.children;
	children.sort(function(a, b){return a.order - b.order});
	for (var i=0; i<children.length; i++) {
		var child = children[i];
		if (child && _model[child.type].category==category) {
			var attributes = child["attributes"];
			html += "<tr>";
			html += "<td><table style=\"border:0px\"><tr>";
			html += "<td style=\"border:0px\"><input type='button' value='x' onclick='javascript:deleteTableEntry("+child.id+")'></input></td>";
			html += "<td style=\"border:0px\"><input type='button' value='sel' onclick='javascript:selectElement("+child.id+",\"Attributes\")'></input></td>";
			html += "</tr></table></td>";
			for (var key in _model[category].attributes) {
				var attr = _model[category].attributes[key];
				if (attr.type != "Display" && attr.type != "Textarea" && attr.type != "StaticMultiSelect" && attr.type != "DynamicMultiSelect" && attr.class != "ClassAttribute") {
					var meta = _model[category]["attributes"][key];
					var value = child.attributes[key];
					if (!value) { value = ""; }
					html += "<td>";
					// html += value;
					html += makeTableInputField(child, key, editPermission);
					html += "</td>";
				}
			}
			html += "</tr>";
		}
	}
	if (_model[category].isCategory==false) {
		html += "<tr><td><input type='button' value='+' onclick=\"javascript:newElementTableEntry("+id+",'"+category+"')\"></input></td></tr>";
	}
	html += "</table>";
	html += "</div>";
	return html
}

function selectCategory(categoryName, numberOfElements) {
	_selection = null;
	_folderSelection = categoryName;
	renderSelectedNavItem(categoryName);
	var html = "<br/><br/><br/><br/><br/><br/><br/>";
	html += "" + numberOfElements + " elements of " + categoryName;
	document.getElementById("content").innerHTML = html;
	updateToolbar();
}

function renderSelectedNavItem(renderId) {
	if (_old_selection_id) {
		var navItem = document.getElementById("nav_"+_old_selection_id);
		if (navItem) {
			navItem.className = "navUnselected";
		}
	}
	if (document.getElementById("nav_"+renderId)) {
		document.getElementById("nav_"+renderId).className = "navSelected";
	}
	_old_selection_id = renderId;
}

function clearContent() {
	_selection = null;
	document.getElementById("content").innerHTML = "";
	updateToolbar();
}

function updateContentStatus() {
	if (_selection) {
		if (_editor=="Attributes") {
			//
			// UPDATE ATTRIBUTES EDITOR
			//
			var id = _selection.id;
			var type = _selection["type"];
			for (var key in _model[type].attributes) {
				var attributeName = key;
				if (document.getElementById("attribute_"+id+"."+attributeName)) {
					var meta = _model[type]["attributes"][attributeName];
					var depResolved = hasDependenciesResolved(_selection, attributeName);
					var disabled = meta["readonly"] ? true : !depResolved;
					document.getElementById("attribute_"+id+"."+attributeName).disabled=disabled;
					if (!disabled) {
						var messages = validate(_selection, attributeName)
						if (messages.length>0) {
							document.getElementById("val_"+id+"."+attributeName).innerHTML=makeErrorBox(messages);
							document.getElementById("val_"+id+"."+attributeName).style.display = "block";
							document.getElementById("attribute_"+id+"."+attributeName).style.border = "1px solid red";
						} else {
							document.getElementById("val_"+id+"."+attributeName).innerHTML="";
							document.getElementById("val_"+id+"."+attributeName).style.display = "none";
							document.getElementById("attribute_"+id+"."+attributeName).style.border = "1px solid black";
						}
					} else {
						document.getElementById("val_"+id+"."+attributeName).innerHTML="";
						document.getElementById("val_"+id+"."+attributeName).style.display = "none";
						document.getElementById("attribute_"+id+"."+attributeName).style.border = "1px solid black";
					}
				}
			}
		} else {
			//
			// UPDATE TABLE EDITOR STATUS
			//
			var type = _editor;
			for (var i=0; i<_selection.children.length; i++) {
				if (_model[_selection.children[i].type].category==type) {
					var element = _selection.children[i];
					var id = element.id;
					//
					for (var key in _model[type].attributes) {
						var attributeName = key;
						if (document.getElementById("attribute_"+id+"."+attributeName)) {
							var meta = _model[type]["attributes"][attributeName];
							var depResolved = hasDependenciesResolved(element, attributeName);
							var disabled = meta["readonly"] ? true : !depResolved;
							document.getElementById("attribute_"+id+"."+attributeName).disabled=disabled;
							if (!disabled) {
								var messages = validate(element, attributeName)
								if (messages.length>0) {
									document.getElementById("attribute_"+id+"."+attributeName).style.border = "1px solid red";
									if (meta.type=="Boolean") {
										document.getElementById("attribute_"+id+"."+attributeName).parentElement.style.backgroundColor = "red";
									}
								} else {
									document.getElementById("attribute_"+id+"."+attributeName).style.border = "1px solid black";
									document.getElementById("attribute_"+id+"."+attributeName).parentElement.style.backgroundColor = "";
								}
							} else {
								document.getElementById("attribute_"+id+"."+attributeName).style.border = "1px solid black";
								document.getElementById("attribute_"+id+"."+attributeName).parentElement.style.backgroundColor = "";
							}
						}
					}
				}
			}
		}
	}
}

function makeErrorBox(messages) {
	var html = "";
	for (var i=0; i<messages.length; i++) {
		if (i>0) { html += "<br/>"; }
		html += messages[i];
	}
	return html;
}

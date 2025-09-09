function resolveVariable(variable, element, key) {
	var result = false;
	if (variable.check.thatElement) {
		variable.check.thatElement = element;
	}
	
	switch (variable.name) {
		case "Lookup":
			result = variableLookup(variable, element, key);
	        break;
		default:
			result = "";
	}
	var resultArray = result.split("|");;
	console.log("* " + resultArray + ", " + resultArray.length);
	if (resultArray.length>1) {
		result = "<ul>";
		for (var i=0; i<resultArray.length; i++) {
			result = result + "<li><b>&#8226; </b>" + resultArray[i] + "</li>";
		}
		result = result + "</ul>";
	}
	return result;
}

function variableLookup(variable, element, key) {
	// find starting point in tree:
	for (var i=0; i<variable.parentCounter; i++) {
    	element = getElement(_data, element.parentId);
	}
    // Lookup by type or use starting point:
    var elementList = [];
	if (variable.type) {
		var lookupList = lookupElement(element, variable.type);
		console.log(lookupList)
    	for (var i=0; i<lookupList.length; i++) {
            if ((!variable.check) || resolveCheck(variable.check, lookupList[i], null)) {
                // Array.prototype.push.apply(elementList, lookupList[i]);
                elementList.push(lookupList[i]);
            }
        }
    } else {
    	Array.prototype.push.apply(elementList, element);
	}
    // process values:
	var result = "";
	var sep = "";
	for (var i=0; i<elementList.length; i++) {
        result = result + sep;
        for (var v=0; v<variable.valueList.length; v++) {
			var resultElement = elementList[i];
        	var lookupValue = variable.valueList[v];
        	var value = "";
        	if (lookupValue.value) {
				value = lookupValue.value;
			} else {
				var level = lookupValue.level;
				for (var l=0; l<level; l++) {
					resultElement = getElement(_data, resultElement.parentId);
				}
				value = resultElement.attributes[lookupValue.attribute];
			}
	        result = result + value;
    	}
        sep = "|";
    }
	return result;
}

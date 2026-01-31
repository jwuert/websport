var _regexInt = new RegExp("^\\d+$");
var _regexDouble = new RegExp("^[0-9]{1,13}(\\.[0-9]*)?$");
	
function validate(element, key) {
	var errors = [];
	var type = element["type"];
	var meta = _model[type]["attributes"][key];
	var value = element.attributes[key];
	if (meta.required && !value) {
		errors.push("required");
	}
	if (meta.type=="Integer" && value!=null && value!="" && !_regexInt.test(value)) {
		errors.push("invalid integer value");
	} else if (meta.type=="Double" && value!=null && value!="" && !_regexDouble.test(value)) {
		errors.push("invalid double value");
	}
	for (var i=0; i<meta.validation.length; i++) {
		var check = meta.validation[i];
		var msg = null;
		var result = resolveCheck(check, element, key);
		if (!result) {
			errors.push(check.message);
		}
	}
	return errors;
}

function hasDependenciesResolved(element, key) {
	var result = true;
	var type = element["type"];
	var meta = _model[type]["attributes"][key];
	var value = element.attributes[key];
	var dependencies = meta["dependencies"];
	for (var i=0; i<dependencies.length; i++) {
		var check = dependencies[i];
		result = result && resolveCheck(check, element, key);
		if (!result) {
			break;
		}
	}
	return result;
}

function resolveCheck(check, element, key) {
	var result = false;
	switch (check.name) {
	case "Completeness":
		result = checkCompleteness(check, element, key);
		break;
    case "IdAvailableCheck":
		result = checkIdAvailableCheck(check, element, key);
		break;
	case "Equality":
		result = checkEquality(check, element, key);
		break;
	case "AttributeEquality":
		result = checkAttributeEquality(check, element, key);
		break;
	case "ParentAttributeEquality":
		result = checkParentAttributeEquality(check, element, key);
		break;
	case "AttributeAttributeEquality":
		result = checkAttributeAttributeEquality(check, element, key);
		break;
	case "Comparison":
		result = checkComparison(check, element, key);
		break;
	case "RangeValidation":
		result = checkRangeValidation(check, element, key);
		break;
	case "ConditionalMatch":
		result = checkConditionalMatch(check, element, key);
		break;
	case "Truth":
		result = checkTruth(check, element, key);
		break;
	case "AttributeTruth":
		result = checkAttributeTruth(check, element, key);
		break;
	case "OrderEquality":
		result = checkOrderEquality(check, element, key);
		break;
	case "Not":
		result = checkNot(check, element, key);
		break;
	case "And":
		result = checkAnd(check, element, key);
		break;
	case "Or":
		result = checkOr(check, element, key);
		break;
	case "AttributeContains":
		result = attributeContains(check, element, key);
		break;
	default:
		result = true;
	}
	return result;
}

function checkNot(check, element, key) {
	//
	// properties innerCheck
	//
	// var innerCheck = JSON.parse(check.innerCheck);
	var innerCheck = parseString(check.innerCheck);
	var result = resolveCheck(innerCheck, element, key);
	return (!result);
}

function checkAnd(check, element, key) {
	//
	// properties innerCheck1, innerCheck2
	//
	var innerCheck1 = parseString(check.innerCheck1);
	var innerCheck2 = parseString(check.innerCheck2);
	var result1 = resolveCheck(innerCheck1, element, key);
	var result2 = resolveCheck(innerCheck2, element, key);
	return result1 && result2;
}

function checkOr(check, element, key) {
	//
	// properties innerCheck1, innerCheck2
	//
	var innerCheck1 = parseString(check.innerCheck1);
	var innerCheck2 = parseString(check.innerCheck2);
	var result1 = resolveCheck(innerCheck1, element, key);
	var result2 = resolveCheck(innerCheck2, element, key);
	return result1 || result2;
}

function checkCompleteness(check, element, key) {
	// completeness of the given attribute of the same element (or key, meaning: itself)
	//
	// properties: attribute
	//
	var attributeName = check.attribute;
	var attributeValue;
	if (attributeName) {
		attributeValue = element.attributes[attributeName];
	} else {
	    attributeValue = element.attributes[key];	
	}
	return attributeValue!=null && attributeValue!="";
}

function checkIdAvailableCheck(check, element, key) {
    // checks if the value is an actual id in the data model
    //
    // properties: type
    //
    var type = check.type;
    var result = false;
    var attributeValue = element.attributes[key];
    if (type) {
        var list = lookupElement(_data, type);
        for (var i=0; i<list.length; i++) {
            if (list[i].attributes["id"]===attributeValue) {
                result = true;
                break;
            }
        }
    } else {
        var list = lookupElementById(_data, attributeValue);
        if (list.length>0) {
            result = true;
        }
    }
    return result;
}

function checkComparison(check, element, key) {
	//
	// properties: attribute, value, comparator
	//
	var attributeName = check.attribute;
	var attributeValue = Number( element.attributes[attributeName] );
	var value = Number( check.value );
	var comp = check.comparator;
	switch (comp) {
	case "EQ": result = (attributeValue==value); break;
	case "NE": result = (attributeValue!=value); break;
	case "LT": result = (attributeValue<value); break;
	case "LE": result = (attributeValue<=value); break;
	case "GT": result = (attributeValue>value); break;
	case "GE": result = (attributeValue>=value); break;
	default: result = false;
	}
	// console.log("* " + attributeValue + ", " + value + ", " + (attributeValue==value) + ", " + (attributeValue===value))
	return result;
}

// --

function checkRangeValidation(check, element, key) {
	//
	// properties: min, max
	//
	var min = Number(check.min);
	var max = Number(check.max);
	var value = element.attributes[key];
	return (value>=min) && (value<=max);
}

function checkConditionalMatch(check, element, key) {
	//
	// properties: conditionalAttribute, value (a json map)
	//
	// var valMap = JSON.parse(check.value);
	valMap = parseString(check.value);
	var conditionalValue = element.attributes[check.conditionalAttribute];
	var pattern = valMap[conditionalValue];
	var re = new RegExp(pattern);
	return (re.test(element.attributes[key]));
}

function checkTruth(check, element, key) {
	//
	// properties: -
	//
	var value = (element.attributes[key] == 'true' || element.attributes[key] == true);
	return value;
}

function checkAttributeTruth(check, element, key) {
	//
	// properties: attribute
	//
	var value = (element.attributes[check.attribute] == 'true' || element.attributes[check.attribute] == true);
	return value;
}

function checkEquality(check, element, key) {
	//
	// properties: value
	//
	var value = check.value;
	var attributeValue = element.attributes[key];
	return (attributeValue==value);
}

function checkAttributeEquality(check, element, key) {
	//
	// properties: attribute, value
	//
	var value = check.value;
	var attributeValue = element.attributes[check.attribute];
	return (attributeValue==value);
}

function checkParentAttributeEquality(check, element, key) {
	//
	// properties: attribute, value
	//
	var value = check.value;
	var parentElement = getElement(_data, element.parentId);
	var attributeValue = parentElement.attributes[check.attribute];
	// console.log("parentAttributeEquality: " + attributeValue + "==" + value + " ? " + (attributeValue==value));
	return (attributeValue==value);
}

function checkAttributeAttributeEquality(check, element, key) {
	//
	// properties: attribute, thatElement, thatAttribute
	//
	var attributeValue = element.attributes[check.attribute];
	var thatAttributeValue = check.thatElement.attributes[check.thatAttribute];
	return (attributeValue==thatAttributeValue);
}

function checkOrderEquality(check, element, key) {
	//
	// properties: order
	//
	// console.log("orderEquality: " + element.order + "==" + check.order + " ? " + (element.order==check.order));
	return element.order==check.order;
}

function attributeContains(check, element, key) {
	//
	// properties: listAttribute, thatElement, thatAttribute
	//
	var list = element.attributes[check.listAttribute];
	var thatValue =check.thatElement.attributes[check.thatAttribute];
	console.log("??? " + thatValue + " ~ " + list);
	return ((","+list+",").includes(","+thatValue+","));
}

function parseString(input) {
    const obj = Object.fromEntries(
      input
        .slice(1, -1) // remove { }
        .split(/,\s*(?=[^=]+=)/) // split on commas before next key
        .map(pair => {
          const [key, ...rest] = pair.split("=");
          return [key, rest.join("=")];
        })
    );
    return obj;
}



package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Stateless;

import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.core.Validation;
import org.wuerthner.sport.core.ValidationResult;
import org.wuerthner.sport.json.SpeedyJson;

@Stateless
public class ValidateWebAction implements ValidateWebActionInterface {
	
	@Override
	public String getId() {
		return "validate";
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String, Object> resultMap = new HashMap<>();
		if (modelState.hasAuxiliaryElement()) {
			ModelElement auxElement = modelState.getAuxiliaryElement();
			ValidationResult result = new Validation().validate(auxElement);
			resultMap.put("command", "validationResult");
			resultMap.put("data", SpeedyJson.createValidationResult(result));
		} else {
			resultMap.put("command", "info");
			resultMap.put("header", "error");
			resultMap.put("message", "No root element given!");
		}
		return resultMap;
	}
}

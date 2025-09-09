package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Stateless;

import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.Model;
import org.wuerthner.sport.core.ModelState;

@Stateless
public class DumpWebAction implements DumpWebActionInterface {
	
	@Override
	public String getId() {
		return "dump";
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String, Object> resultMap = new HashMap<>();
		if (modelState.hasAuxiliaryElement()) {
			System.out.println(Model.makeString(modelState.getAuxiliaryElement()));
			resultMap.put("command", "donothing");
		} else {
			resultMap.put("command", "info");
			resultMap.put("header", "error");
			resultMap.put("message", "No data given!");
		}
		return resultMap;
	}
}

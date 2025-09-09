package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.json.SpeedyJson;
import org.wuerthner.sport.persistence.dao.GenericDao;

@Stateless
public class SaveDocumentWebAction implements SaveDocumentWebActionInterface {
	
	@Inject
	private GenericDao dao;
	
	@Override
	public String getId() {
		return "save";
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String, Object> resultMap = new HashMap<>();
		if (modelState.hasAuxiliaryElement()) {
			ModelElement root = modelState.getAuxiliaryElement();
			dao.persistTree(root);
			resultMap.put("command", "setData");
			resultMap.put("data", SpeedyJson.create(root));
		} else {
			resultMap.put("command", "info");
			resultMap.put("header", "error");
			resultMap.put("message", "No root element given!");
		}
		return resultMap;
	}
}

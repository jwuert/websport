package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.wuerthner.sport.api.Attribute;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.json.SpeedyJson;
import org.wuerthner.sport.persistence.dao.GenericDao;
import org.wuerthner.sport.persistence.dao.GenericDao.DocumentReference;

@Stateless
public class NewRootElementWebAction extends NewRootElementAction implements NewRootElementWebActionInterface {
	@Inject
	public GenericDao dao;
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String, Object> resultMap = new HashMap<>();
		String name = parameterMap.get("name");
		if (StringUtils.isNotBlank(name)) {
			List<DocumentReference> documents = dao.getDocuments(factory.getRootElementType());
			if (documents.stream().filter(ref -> ref.NAME.equals(name)).count() > 0) {
				resultMap.put("command", "info");
				resultMap.put("header", "error");
				resultMap.put("message", "Name '" + name + "' already exists!");
			} else {
				ModelElement newRootElement = factory.createElement(factory.getRootElementType());
				newRootElement.performTransientSetAttributeValueOperation((Attribute<String>) newRootElement.getAttribute("id"), name);
				dao.persistTree(newRootElement);
				dao.flush();
				JsonObject jsonElement = SpeedyJson.create(newRootElement);
				
				resultMap.put("command", "setData");
				resultMap.put("data", jsonElement);
			}
		}
		return resultMap;
	}
}

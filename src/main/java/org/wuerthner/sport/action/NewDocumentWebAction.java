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

@Deprecated
@Stateless
public class NewDocumentWebAction implements NewDocumentWebActionInterface {
	
	@Inject
	private GenericDao dao;
	
	@Inject
	public ModelElementFactory factory;
	
	@Override
	public String getId() {
		return "newDocument";
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String, Object> resultMap = new HashMap<>();
		String name = parameterMap.get("name");
		System.out.println("=> " + name);
		
		if (StringUtils.isNotBlank(name)) {
			List<DocumentReference> documents = dao.getDocuments(factory.getRootElementType());
			if (documents.stream().filter(ref -> ref.NAME.equals(name)).count() > 0) {
				System.out.println("name already exists: " + name);
				// send error message
			} else {
				ModelElement newElement = factory.createElement(factory.getRootElementType());
				newElement.performTransientSetAttributeValueOperation((Attribute<String>) newElement.getAttribute("id"), name);
				dao.persistTree(newElement);
				
				JsonObject jsonElement = SpeedyJson.create(newElement);
				
				resultMap.put("command", "setData");
				resultMap.put("data", jsonElement);
			}
		} else {
			resultMap.put("command", "info");
			resultMap.put("header", "error");
			resultMap.put("message", "insufficient parameters: " + name);
		}
		return resultMap;
	}
	
}

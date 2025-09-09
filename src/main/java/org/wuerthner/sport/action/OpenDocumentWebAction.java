package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.persistence.dao.GenericDao;

@Stateless
public class OpenDocumentWebAction implements OpenDocumentWebActionInterface {
	public static final String NAME = "openDocument";
	
	@Inject
	private GenericDao dao;
	
	@Override
	public String getId() {
		return NAME;
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		long id = Long.valueOf(parameterMap.get("id"));
		ModelElement element = dao.getElement(id);
		// JsonObjectBuilder json = Json.createObjectBuilder();
		// json.add("command", "setData");
		// json.add("data", SpeedyJson.create(element));
		// return json.build();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("command", "setData");
		resultMap.put("data", element);
		return resultMap;
	}
}

package org.wuerthner.sport.action;

import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Stateless;

import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;

@Stateless
public class BroadcastWebAction implements BroadcastWebActionInterface {
	
	@Override
	public String getId() {
		return "broadcast";
	}
	
    @Override
    public Map<String,Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String,String> parameterMap) {
    	Map<String, Object> resultMap = new HashMap<>();
		String msg = parameterMap.get("value");
		resultMap.put("command", "info");
		resultMap.put("header", "Broadcast");
		resultMap.put("message", msg);
		return resultMap;
    }
}

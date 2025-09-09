package org.wuerthner.sport.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;

import org.wuerthner.sport.api.Action;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.Delta;
import org.wuerthner.sport.core.ModelState;

@Stateless
public class CompareWebAction extends CompareAction implements CompareWebActionInterface {
	
	@Override
	public String getId() {
		return "compare";
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String,Object> result = new HashMap<>();
		Map<String, Object> invoke = super.invoke(factory, modelState, parameterMap);
		Delta delta = (Delta) invoke.get(Action.DELTA);
		
		List<List<String>> rowList = new ArrayList<>();
		rowList.add(
				Arrays.asList("ID", "Type", "Attribute", "Change", "Original")
		);
		for (Delta.Difference difference : delta.getDifferences()) {
			rowList.add(
				Arrays.asList(
                    difference.fqid, difference.elementType, difference.attributeLabel, difference.value1, difference.value2)
			);
		}
		result.put(Action.DELTA, rowList);
		result.put("command", "compareResult");
		return result;
	}
}

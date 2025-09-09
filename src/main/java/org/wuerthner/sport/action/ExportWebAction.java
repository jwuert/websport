package org.wuerthner.sport.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;

import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.core.XMLWriter;

@Stateless
public class ExportWebAction implements ExportWebActionInterface {
	public static final String EXPORT = "export";
	
	@Inject
	private ServletContext servletContext;
	
	@Override
	public String getId() {
		return "export";
	}
	
	@Override
	public boolean requiresData() {
		return true;
	}
	
	@Override
	public Map<String, Object> invoke(ModelElementFactory factory, ModelState modelState, Map<String, String> parameterMap) {
		Map<String, Object> resultMap = new HashMap<>();
		String fileName = "";
		if (modelState.hasRootElement()) {
			try {
				XMLWriter xmlWriter = new XMLWriter();
				File serverDir = new File(getServerRootDirectory());
				if (!serverDir.exists()) {
					serverDir.mkdirs();
				}
				fileName = "export.xml";
				File outputFile = new File(serverDir, fileName);
				OutputStream os = new FileOutputStream(outputFile);
				xmlWriter.run(modelState.getRootElement(), os);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			resultMap.put("command", "action");
			resultMap.put("action", getId());
			resultMap.put("message", "<a href=\"../"+EXPORT+"/" + fileName + "\" target=\"_blank\">Export Model</a>");
		} else {
			resultMap.put("command", "info");
			resultMap.put("header", "error");
			resultMap.put("message", "No root element given!");
		}
		return resultMap;
		
	}
	
	public String getServerRootDirectory() {
		String root = servletContext.getRealPath("/"); // System.getProperty("catalina.base");
		
		if (root == null) {
			File tmpFolder;
			try {
				tmpFolder = Files.createTempDirectory("sport-").toFile();
				System.out.println("no webserver root directory set: 'catalina.base' is null! Using '" + tmpFolder + "' instead!");
				root = tmpFolder.getAbsolutePath();
				tmpFolder.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			System.out.println("found webserver root directory: '" + root + "'");
		}
		return root + EXPORT;
	}
}

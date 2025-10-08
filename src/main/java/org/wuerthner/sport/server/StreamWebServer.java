package org.wuerthner.sport.server;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import jakarta.xml.bind.DatatypeConverter;

import org.wuerthner.sport.action.OpenDocumentWebAction;
import org.wuerthner.sport.api.Action;
import org.wuerthner.sport.api.ActionProvider;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.ModelElementFactory;
import org.wuerthner.sport.core.Model;
import org.wuerthner.sport.core.ModelState;
import org.wuerthner.sport.core.XMLReader;
import org.wuerthner.sport.json.JsonToModel;
import org.wuerthner.sport.json.SpeedyJson;
import org.wuerthner.sport.persistence.dao.ConcurrencyException;
import org.wuerthner.sport.persistence.dao.GenericDao;
import org.wuerthner.sport.persistence.dao.GenericDao.DocumentReference;
import org.wuerthner.sport.util.Logger;

// @ApplicationScoped
@Stateless
@ServerEndpoint(value = "/socket", configurator=QueryConfigurator.class)
public class StreamWebServer {
	private final static Logger logger = Logger.getLogger(StreamWebServer.class);
	
	@Inject
	public ModelElementFactory factory;
	
	@Inject
	public JsonToModel jsonToModel;
	
	@Inject
	public GenericDao dao;
	
	@Inject
	private ActionProvider actionProvider;
	
	public StreamWebServer() {
		logger.info("Initializing StreamServer...");
	}
	
	static List<Session> sessionList = new ArrayList<>();
	
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws Exception {
		logger.info("Server.onOpen(), sessionId=" + (session != null ? session.getId() : "(null session)"));
        HttpSession httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        Boolean justLoggedIn = false;
        if (httpSession!=null) {
            if (httpSession.getAttribute("justLoggedIn")!=null) {
                justLoggedIn = (Boolean) httpSession.getAttribute("justLoggedIn");
            }
            if (Boolean.TRUE.equals(justLoggedIn)) {
                httpSession.setAttribute("justLoggedIn", Boolean.FALSE);
            }
        }
		if (session != null) {
            String userId = (session.getUserPrincipal()!=null ? session.getUserPrincipal().getName() : dao.getUserIdByUUID(""+session.getUserProperties().get("user")));
            System.out.println("User ID: " + userId + " logged in via: " + (session.getUserPrincipal()==null?"UUID":"Authentication"));
            System.out.println("Just logged in: " + justLoggedIn);
            Map<String,String> userMap = dao.getUserMap();
            if (userMap.get(userId)==null) {
                // if userId is not in our user-map, then close session:
                JsonObjectBuilder jsonModel = Json.createObjectBuilder();
                jsonModel.add("command", "info");
                jsonModel.add("header", "info");
                jsonModel.add("message", "User '" + ("null".equals(session.getUserProperties().get("user")) ? "?" : session.getUserProperties().get("user")) + "' not registered!");
                session.getBasicRemote().sendText(jsonModel.build().toString());
                session.close();
            } else {
                if (session.getUserPrincipal() != null) {
                    // editing mode - if there already is someone else editing, close session!
                    String maintenance = null;
                    for (Session s : sessionList) {
                        if (s.getUserPrincipal() != null) {
                            maintenance = s.getUserPrincipal().getName();
                        }
                    }
                    System.out.println("///// user: " + userId + ", maintenance: " + maintenance);
                    if (maintenance != null && !maintenance.equals(userId)) {
                        System.out.println("Session will be closed due to maintenance");
                        JsonObjectBuilder jsonModel = Json.createObjectBuilder();
                        jsonModel.add("command", "info");
                        jsonModel.add("header", "Wartungsarbeiten");
                        jsonModel.add("message", "Das System wird gerade von " + maintenance + " bearbeitet!");
                        session.getBasicRemote().sendText(jsonModel.build().toString());
                        session.close();
                    }
                }
                if (session.isOpen()) {
                    sessionList.add(session);
                    JsonObjectBuilder jsonUserMap = Json.createObjectBuilder();
                    for (Map.Entry<String, String> entry : userMap.entrySet()) {
                        jsonUserMap.add(entry.getKey(), entry.getValue());
                    }

                    // send data model:
                    JsonObjectBuilder jsonModel = Json.createObjectBuilder();
                    jsonModel.add("command", "setModel");
                    jsonModel.add("data", SpeedyJson.createModel(factory));
                    jsonModel.add("appName", factory.getAppName());
                    jsonModel.add("userMap", jsonUserMap.build());
                    jsonModel.add("newSession", justLoggedIn);
                    sendMessage(session, jsonModel.build());

                    sendDocumentList(session);
                    sendActionList(session);
                    // send checks:
                    // JsonObjectBuilder jsonC = Json.createObjectBuilder();
                    // jsonC.add("command", "setChecks");
                    // JsonObjectBuilder map = Json.createObjectBuilder();
                    // String script = new Truth().getScript().replaceFirst("function [A-Za-z]+", "function ");
                    // System.out.println(script);
                    // map.add("getTruth", script);
                    // script = new AttributeTruth().getScript().replaceFirst("function [A-Za-z]+", "function ");
                    // map.add("getAttributeTruth", script);
                    // jsonC.add("data", map.build());
                    // sendMessage(session, jsonC.build());
                }
            }
		}
	}
	
	private void sendDocumentList(Session session) {
		// send available documents:
		List<DocumentReference> refList = dao.getDocuments(factory.getRootElementType());
		JsonObjectBuilder json = Json.createObjectBuilder();
		json.add("command", "setDocumentList");
		json.add("data", SpeedyJson.createDocumentMap(refList));
		sendMessage(session, json.build());
	}
	
	private void sendActionList(Session session) {
		JsonObjectBuilder json = Json.createObjectBuilder();
		json.add("command", "setActionList");
		JsonArrayBuilder jsonArray = Json.createArrayBuilder();
		for (Action action : actionProvider.getActionList()) {
			JsonObjectBuilder jsonAction = Json.createObjectBuilder();
			jsonAction.add("id", action.getId());
			jsonAction.add("requiresData", action.requiresData());
			jsonAction.add("parameterList", SpeedyJson.createJsonParameterList(action.getParameterList(null), null));
			jsonArray.add(jsonAction.build());
		}
		json.add("actionList", jsonArray.build());
		actionProvider.getIdList();
		sendMessage(session, json.build());
	}
	
	@OnClose
	public void onClose(Session session) throws Exception {
		if (session != null) {
//            JsonObjectBuilder jsonModel = Json.createObjectBuilder();
//            jsonModel.add("command", "nocommand");
//            sendMessage(session, jsonModel.build());
			sessionList.remove(session);
			logger.info("Server.onClose(), sessionId=" + (session != null ? session.getId() : "(null session)"));
		}
	}
	
	@OnMessage
	public void onMessage(String jsonString, Session session) throws Exception {
		logger.info("Server.onMessage(): " + jsonString);
		if (session != null) {
			if (jsonString.length()>25 && jsonString.substring(0, 25).indexOf("transfer") > 0) {
				//
				// FILE TRANSFER (import)
				//
				String input = jsonString.substring(jsonString.indexOf("base64") + 7, jsonString.length() - 2);
				byte[] binary = DatatypeConverter.parseBase64Binary(input);
				// String xmlData = new String(binary);
				XMLReader reader = new XMLReader(factory, factory.getRootElementType(), "root");
				ModelElement root = reader.run(new ByteArrayInputStream(binary));
				dao.persistTree(root);
				// System.out.println("Imported file! " + root.getName());
				Optional<Action> optAction = actionProvider.getAction(OpenDocumentWebAction.NAME);
				if (optAction.isPresent()) {
					Map<String, String> parameterMap = new HashMap<>();
					parameterMap.put("id", "" + root.getTechnicalId());
					// JsonObjectBuilder json = Json.createObjectBuilder();
					// json.add("id", root.getTechnicalId());
					Map<String, Object> resultMap = optAction.get().invoke(factory, new ModelState(root), parameterMap);
					JsonObject response = SpeedyJson.createCommandMessageFromMap(resultMap);
					sendMessage(session, response);
				}
			} else {
				JsonReader reader = Json.createReader(new StringReader(jsonString));
				try {
					JsonObject jsonObject = reader.readObject();
					String command = jsonObject.getString("command");
					logger.info("Server.onMessage(), command: " + command);
					Optional<Action> actionOptional = actionProvider.getAction(command);
                    String error = null;
					if (actionOptional.isPresent()) {
						ModelElement rootElement = jsonObject.containsKey("rootId") ? dao.getElement(Long.valueOf(jsonObject.getInt("rootId"))) : null;
						ModelElement selectedElement = jsonObject.containsKey("selectedId") && Long.valueOf(jsonObject.getInt("selectedId")) >= 0 ? dao.getElement(Long.valueOf(jsonObject.getInt("selectedId"))) : null;
						ModelElement auxElement = null;
						if (jsonObject.containsKey("data")) {
							JsonObject data = jsonObject.getJsonObject("data");
							long id = data.getInt("id");
							auxElement = dao.getElement(id);
							Map<Integer, ModelElement> idMap = new HashMap<>();
                            jsonToModel.mergeTree(auxElement, data, idMap);
                            if (!command.equals("save")) {
								for (Map.Entry<Integer, ModelElement> entry : idMap.entrySet()) {
									// sets back the technical ID from the UI (negative numbers)
									// the elements cannot be stored in the database now, but the validation links will work!
									entry.getValue().setTechnicalId(entry.getKey());
								}
							}
						}
						Map<String, String> parameterMap = SpeedyJson.jsonToMap(jsonObject);
						parameterMap.put(Model.REFERENCE_FILE, null);
						System.out.println("root: " + rootElement);
                        Map<String, Object> resultMap = new HashMap<>();
                        try {
						    resultMap = actionOptional.get().invoke(factory, new ModelState(rootElement, selectedElement, auxElement), parameterMap);
                        } catch (EJBException e) {
                            error = "Bitte erneut versuchen!";
                        }
                        resultMap.put("senderId", session.getUserPrincipal()!=null ? session.getUserPrincipal().getName() : dao.getUserIdByUUID(""+session.getUserProperties().get("user")));
						System.out.println("ResultMap: " + resultMap);
						JsonObject response = SpeedyJson.createCommandMessageFromMap(resultMap);
						sendMessage(session, response);
                        if (error!=null) {
                            JsonObjectBuilder jsonModel = Json.createObjectBuilder();
                            jsonModel.add("command", "info");
                            jsonModel.add("header", "info");
                            jsonModel.add("message", error);
                            session.getBasicRemote().sendText(jsonModel.build().toString());
                        }
					} else {
						logger.error("Server.onMessage(), no action found for '" + command + "'");
					}
                    if (command.equals("logoutAll")) {
                        logger.info("Closing all sessions!");
                        for (Session s : List.copyOf(sessionList)) {
                            s.close();
                        }
                    }
				} catch (JsonParsingException e) {
					logger.error(e);
				}
			}
		}
	}
	
	@OnError
	public void onError(final Throwable throwable) {
		logger.error("system: *ERROR* " + throwable.getMessage());
		throwable.printStackTrace();
	}
	
	public void sendMessage(Session session, JsonObject message) {
		synchronized (session) {
			if (session.isOpen()) {
				try {
					// TODO: should be async remote, but did not work, despite "synchronized", gave this exception:
					// IllegalStateException: The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method
					// session.getAsyncRemote().sendText(message.toString());
					// session.getBasicRemote().sendText(message.toString());
					if (!sessionList.contains(session))
						throw new RuntimeException("### Session not in sessionList!");
					int count = 1;
                    //Set<String> userIdSet = sessionList.stream().map(s ->
                    //        s.getUserPrincipal() != null ? s.getUserPrincipal().getName() : dao.getUserIdByUUID("" + s.getUserProperties().get("user"))
                    //).collect(Collectors.toSet());


                    String user = (session.getUserPrincipal()!=null ? session.getUserPrincipal().getName() : dao.getUserIdByUUID(""+session.getUserProperties().get("user")));
                    for (Session s : List.copyOf(sessionList)) {
						logger.info("+++ Session: " + count++ + " - " + session + ", " + user);
						s.getBasicRemote().sendText(message.toString());
                        // send userId
                        JsonObjectBuilder jsonModel = Json.createObjectBuilder();
                        jsonModel.add("command", "setUserId");
                        jsonModel.add("userId", (s.getUserPrincipal()!=null ? s.getUserPrincipal().getName() : dao.getUserIdByUUID(""+s.getUserProperties().get("user"))));
                        jsonModel.add("maintenance", session.getUserPrincipal()!=null ? session.getUserPrincipal().getName() : "");
                        s.getBasicRemote().sendText(jsonModel.build().toString());
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

}

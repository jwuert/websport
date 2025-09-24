package org.wuerthner.sport.server;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import java.security.Principal;

public class QueryConfigurator extends ServerEndpointConfig.Configurator {
    // public QueryConfigurator() {}
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        String username = request.getParameterMap()
                .getOrDefault("login", java.util.List.of("anonymous"))
                .get(0);

        // Store it in user properties
        config.getUserProperties().put("user", username);

        // Optionally, set a Principal so getUserPrincipal() works
        Principal userPrincipal = new Principal() {
            @Override
            public String getName() {
                return username;
            }
        };
        //config.getUserProperties().put(Principal.class.getName(), userPrincipal);
    }
}


package org.wuerthner.sport.server;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/") // base path for all JAX-RS resources
public class RestApplication extends Application {
}

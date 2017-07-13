package net.twentyonesolutions.lucee.websocket;

import lucee.commons.io.log.Log;
import lucee.runtime.exp.PageException;
import net.twentyonesolutions.lucee.app.LuceeApp;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by Igal on 10/2/2016.
 */
public class Configurator {

	public static void configureEndpoint(String endpointPath, Class endpointClass, Class handshakeHandlerClass,
			LuceeApp app) throws ClassNotFoundException, IllegalAccessException, InstantiationException,
			DeploymentException, PageException {

		ServerEndpointConfig serverEndpointConfig = ServerEndpointConfig.Builder.create(endpointClass,
				endpointPath).configurator(
						(ServerEndpointConfig.Configurator) handshakeHandlerClass.newInstance()).build();

		try {

			ServerContainer serverContainer = (ServerContainer) app.getServletContext().getAttribute(
					"javax.websocket.server.ServerContainer");
			serverContainer.addEndpoint(serverEndpointConfig);
		}
		catch (DeploymentException ex) {

			app.log(Log.LEVEL_DEBUG, "Failed to register endpoint " + endpointPath + ": " + ex.getMessage(),
					app.getName(), "websocket");
		}
		// System.out.println(Configurator.class.getName() + " >>> exit configureEndpoint()");
	}

}
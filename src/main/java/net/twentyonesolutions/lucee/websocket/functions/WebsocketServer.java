package net.twentyonesolutions.lucee.websocket.functions;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import net.twentyonesolutions.lucee.app.LuceeApp;
import net.twentyonesolutions.lucee.app.LuceeAppListener;
import net.twentyonesolutions.lucee.app.LuceeApps;
import net.twentyonesolutions.lucee.app.RegisterLuceeApp;
import net.twentyonesolutions.lucee.websocket.Configurator;
import net.twentyonesolutions.lucee.websocket.HandshakeHandler;
import net.twentyonesolutions.lucee.websocket.LuceeEndpoint;
import net.twentyonesolutions.lucee.websocket.connections.ConnectionManager;

/**
 * Created by Igal on 12/5/2016.
 */
public class WebsocketServer extends BIF {

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		// System.out.println(args);

		if (!LuceeApps.getDecisionUtil().isSimpleValue(args[0]))
			throw new RuntimeException("Invalid argument, expected a String as the first argument for "
					+ getClass().getSimpleName() + "()");

		if (!LuceeApps.getDecisionUtil().isComponent(args[1]))
			throw new RuntimeException("Invalid argument, expected a Component as the second argument for "
					+ getClass().getSimpleName() + "()");

		// if (args.length > 2){
		//
		// if (!LuceeApps.getDecisionUtil().isStruct(args[2]))
		// throw new RuntimeException("Invalid argument, expected a Struct as the third argument for " +
		// getClass().getSimpleName() + "()");
		//
		// }

		String endpoint = (String) args[0];
		Component listenerComponent = (Component) args[1];

		String key;
		LuceeApp luceeApp;
		LuceeAppListener luceeAppListener;
		ConnectionManager connManager;

		luceeApp = (LuceeApp) RegisterLuceeApp.call(pc);

		// register full key with context root
		key = pc.getServletContext().getRealPath("/") + "@" + endpoint;
		luceeAppListener = LuceeApps.registerListener(luceeApp, listenerComponent, key);
		connManager = ConnectionManager.setAppListener(key, luceeAppListener);

		/*
		 * // register partial key without context root (default, for single context, in case we don't have Filter) key
		 * = "@" + endpoint; luceeAppListener = LuceeApps.registerListener(luceeApp, listenerComponent, key);
		 * connManager = ConnectionManager.setAppListener(key, luceeAppListener); //
		 */

		try {

			Configurator.configureEndpoint(endpoint, LuceeEndpoint.class, HandshakeHandler.class, luceeApp);
		}
		catch (Throwable t) {

			throw LuceeApps.toPageException(t);
		}

		return connManager;
	}

}
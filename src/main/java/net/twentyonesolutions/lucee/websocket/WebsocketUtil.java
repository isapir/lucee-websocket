package net.twentyonesolutions.lucee.websocket;

import lucee.commons.io.log.Log;
import lucee.runtime.Component;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.Application;
import net.twentyonesolutions.lucee.app.LuceeApp;
import net.twentyonesolutions.lucee.app.LuceeAppListener;
import net.twentyonesolutions.lucee.app.LuceeApps;
import net.twentyonesolutions.lucee.app.LuceeAppsUtil;
import net.twentyonesolutions.lucee.websocket.connections.ConnectionManager;

import javax.websocket.Session;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Igal on 10/2/2016.
 */
public class WebsocketUtil {

	public static final String KEY_WEBSOCKET_WRAPPER = WebSocket.class.getCanonicalName();

	public static final Collection.Key KEY_WEBSOCKET_ID = LuceeApps.toKey("websocketId");
	public static final Collection.Key KEY_PATH_PARAMETERS = LuceeApps.toKey("pathParameters");

	public static LuceeAppListener getLuceeAppListener(String listenerKey) {

		return LuceeApps.getAppListener(listenerKey);
	}

	public static LuceeAppListener getLuceeAppListener(javax.websocket.Session wsSession) {

		Map<String, Object> userProps = wsSession.getUserProperties();
		String listenerKey = (String) userProps.get(HandshakeHandler.KEY_LUCEE_APP_KEY);
		return getLuceeAppListener(listenerKey);
	}

	public static ConnectionManager getConnectionManager(javax.websocket.Session wsSession) {

		Map<String, Object> userProps = wsSession.getUserProperties();
		ConnectionManager result = (ConnectionManager) userProps.get(HandshakeHandler.KEY_CONN_MANAGER);
		return result;
	}



	/*
	public static void populateStructFromArray(Struct struct, Object... args){

		LuceeAppsUtil.populateStruct(struct, WebsocketUtil::asWebsocketWrapperIfWebsocket, args);

		Iterator it = struct.getIterator();
		while(it.hasNext()){
			Object o = it.next();
			System.out.println(o);
		}

		if (args.length % 2 != 0)
			throw new IllegalArgumentException("args must be of even length");

		Collection.Key key;
		Object value;

		// args must have even size, where each key is followed by value
		for (int i = 0; i < args.length; i += 2) {

			// even index is the key, which must be either Collection.Key or String
			key = (args[i] instanceof Collection.Key) ?
					(Collection.Key) args[i]
					:
					LuceeApps.toKey((String) args[i]);

			value = args[i + 1];

			// wrap JSR Session(s) as WebSocket(s) before passing them to listener
			if (value instanceof javax.websocket.Session && (!(value instanceof WebSocket)))
				value = WebsocketUtil.asWebsocketWrapper((Session) value);

			struct.setEL(key, value);
		}
	}
	//*/

	public static Object invokeMethodWithNamedArgs(
			 Component component
			,Collection.Key method
			,Object... args) {

		if (LuceeApps.hasMethod(component, method)){

//			luceeApp.log(Log.LEVEL_DEBUG, "calling listener." + method + "()", "websocket", "websocket");

			Struct struct = LuceeApps.getCreationUtil().createStruct();

			LuceeAppsUtil.populateStruct(struct, WebsocketUtil::asWebsocketWrapperIfWebsocket, args);
		}
		else {

//			luceeApp.log(Log.LEVEL_DEBUG, "listener." + method + "() is not implemented", "websocket", "websocket");
		}

		return null;
	}


	public static Object invokeListenerMethodWithNamedArgs(
			 LuceeAppListener appListener
			,Collection.Key method
			,Object... args) {

		if (appListener == null)
			return null;

		LuceeApp luceeApp = appListener.getApp();

		if (LuceeApps.hasMethod(appListener, method)) {

			luceeApp.log(Log.LEVEL_DEBUG, "calling listener." + method + "()", "websocket", "websocket");

			Struct struct = LuceeApps.getCreationUtil().createStruct();

			LuceeAppsUtil.populateStruct(struct, WebsocketUtil::asWebsocketWrapperIfWebsocket, args);

			Object luceeResult = appListener.invokeWithNamedArgs(method, struct);

			// if the listener threw an exception, rethrow it
			if (luceeResult instanceof Exception) {

				luceeApp.log(Log.LEVEL_ERROR, "listener." + method + "() threw an exception: " + luceeResult.toString()
						+ ". stack trace: " + getStackTrace((Exception) luceeResult), "websocket", "websocket");

				// if (rethrowOnError)
				// throw new RuntimeException((Exception) luceeResult);
			}

			return luceeResult;
		}
		else {

			luceeApp.log(Log.LEVEL_DEBUG, "listener." + method + "() is not implemented", "websocket", "websocket");
		}

		return null;
	}

	public static Object invokeListenerMethodWithNamedArgs(
			 javax.websocket.Session session
			,Collection.Key method
			,Object... args) {

		LuceeAppListener appListener = WebsocketUtil.getLuceeAppListener(session);
		return invokeListenerMethodWithNamedArgs(appListener, method, args);
	}

	/**
	 * wraps the JSR Session with WebSocket and returns the wrapper
	 *
	 * @param wsSession
	 * @return
	 */
	public static WebSocket asWebsocketWrapper(javax.websocket.Session wsSession) {

		if (wsSession instanceof WebSocket)
			return (WebSocket) wsSession;

		Map<String, Object> userProps = wsSession.getUserProperties();
		WebSocket websocket = (WebSocket) userProps.get(KEY_WEBSOCKET_WRAPPER);
		if (websocket instanceof WebSocket) // if we already have a wrapper, return it
			return websocket;

		websocket = new WebSocket(wsSession); // wrap the JSR Session and store it in user properties
		userProps.put(KEY_WEBSOCKET_WRAPPER, websocket); // store it for subsequent calls

		return websocket;
	}


	public static Object asWebsocketWrapperIfWebsocket(Object o){

		if (o instanceof WebSocket || o instanceof javax.websocket.Session)
			return asWebsocketWrapper((javax.websocket.Session)o);

		return o;
	}


	/**
	 *
	 * @param wsSession
	 * @return
	 */
	public static Struct getStruct(Session wsSession) {

		return (Struct) wsSession.getUserProperties().get(HandshakeHandler.KEY_LUCEE_STRUCT); // created during
																								// handshake
	}

	/**
	 * returns the Lucee SessionScope associated with the passed Websocket connection
	 *
	 * @param wsSession
	 *            - the JSR Websocket session
	 * @return
	 */
	public static lucee.runtime.type.scope.Session getSessionScope(javax.websocket.Session wsSession) {

		// lucee.runtime.type.scope.Session sessionScope =
		// (lucee.runtime.type.scope.Session)wsSession.getUserProperties().get(HandshakeHandler.KEY_LUCEE_SESSION);

		LuceeAppListener appListener = WebsocketUtil.getLuceeAppListener(wsSession);
		String cfid = (String) wsSession.getUserProperties().get(HandshakeHandler.idCookieName);

		lucee.runtime.type.scope.Session sessionScope = appListener.getApp().getSessionScope(cfid);

		if (sessionScope != null)
			sessionScope.touch(); // keep session alive

		return sessionScope;
	}

	/**
	 * returns the Lucee ApplicationScope associated with the passed Websocket connection
	 *
	 * @param wsSession
	 *            - the JSR Websocket session
	 * @return
	 */
	public static Application getApplicationScope(javax.websocket.Session wsSession) {

		Application applicationScope = WebsocketUtil.getLuceeAppListener(wsSession).getApp().getApplicationScope();
		return applicationScope;
	}

	public static String getStackTrace(Throwable throwable) {

		StringWriter stringWriter = new StringWriter(512);
		throwable.printStackTrace(new PrintWriter(stringWriter));

		return stringWriter.toString();
	}

	/*
	 * public static void log(int logLevel, String message, Session wsSession){
	 *
	 * ConnectionManager connMgr = getConnectionManager(wsSession); connMgr.log(logLevel, message); }
	 *
	 * public static void logDebug(String message, Session session){
	 *
	 * log(LEVEL_DEBUG, message, session); }
	 *
	 * public static void logInfo(String message, Session session){
	 *
	 * log(LEVEL_INFO, message, session); }
	 *
	 * public static void logWarn(String message, Session session){
	 *
	 * log(LEVEL_WARN, message, session); }
	 *
	 * public static void logErr(String message, Session session){
	 *
	 * log(LEVEL_ERROR, message, session); } //
	 */

}
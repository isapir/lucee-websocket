package net.twentyonesolutions.lucee.websocket;

import lucee.commons.io.log.Log;
import lucee.runtime.type.Struct;
import net.twentyonesolutions.lucee.app.LuceeApps;
import net.twentyonesolutions.lucee.websocket.connections.ConnectionManager;

import javax.websocket.*;
import java.io.IOException;
import java.util.Map;

import static net.twentyonesolutions.lucee.websocket.Constants.*;

/**
 * Created by Igal on 10/2/2016.
 */
public class LuceeEndpoint extends Endpoint {

	/**
	 * Event that is triggered when a new WebSocket connection is opened, immediately after
	 * HandshakeHandler.modifyHandshake() assuming that an exception was not thrown during the handshake.
	 *
	 * @param wsSession
	 *            The new session.
	 * @param endpointConfig
	 *            The configuration that was set in HandshakeHandler.modifyHandshake(), which provides access to the
	 *            UserProperties via getUserProperties() and PathParams via EndpointConfig..getPathParamMap()
	 */
	@Override
	public void onOpen(Session wsSession, EndpointConfig endpointConfig) {

		ConnectionManager connMgr = ConnectionManager.getConnectionManager(wsSession);

		WebSocket websocket = WebsocketUtil.asWebsocketWrapper(wsSession);

		connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " enter onOpen()");

		Struct struct = WebsocketUtil.getStruct(wsSession);
		String channel = null;
		String k, v;

		struct.setEL(WebsocketUtil.KEY_WEBSOCKET_ID, wsSession.getId());

		Struct pathParams = LuceeApps.getCreationUtil().createStruct();
		for (Map.Entry<String, String> e : wsSession.getPathParameters().entrySet()) {
			k = e.getKey();
			v = e.getValue();
			pathParams.setEL(LuceeApps.toKey(k), v);

			if (k.equalsIgnoreCase("channel"))
				channel = v;
		}
		struct.put(WebsocketUtil.KEY_PATH_PARAMETERS, pathParams);

		connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " calling listener.onOpen()");

		Object luceeResult = WebsocketUtil.invokeListenerMethodWithNamedArgs(websocket, LISTENER_METHOD_ON_OPEN,
				ARG_WEBSOCKET, websocket, ARG_ENDPOINT_CONFIG, endpointConfig, ARG_SESSION_SCOPE,
				websocket.getSessionScope(), ARG_APPLICATION_SCOPE, websocket.getApplicationScope());

		if (LuceeApps.isBooleanFalse(luceeResult)) {
			// terminate the connection if listener returned false

			connMgr.log(Log.LEVEL_INFO, "connection " + wsSession.getId()
					+ " listener.onOpen() refused the connection by returning false. closing connection.");

			try {
				wsSession.close();
			}
			catch (IOException e) {
			}
		}

		if (channel != null) {
			// if {channel} appears in the path parameters then subscribe the user to that channel
			connMgr.subscribe(channel, websocket);
		}

		wsSession.addMessageHandler(new MessageHandler.Whole<String>() {

			public void onMessage(String message) {

				connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " enter onMessage(); " + message);

				Object luceeResult = WebsocketUtil.invokeListenerMethodWithNamedArgs(websocket,
						LISTENER_METHOD_ON_MESSAGE, ARG_WEBSOCKET, websocket, ARG_MESSAGE, message, ARG_SESSION_SCOPE,
						websocket.getSessionScope(), ARG_APPLICATION_SCOPE, websocket.getApplicationScope());

				connMgr.log(Log.LEVEL_DEBUG, "listener.onMessage() "
						+ (luceeResult == null ? "did not return a value" : "returned: " + luceeResult.toString()));

				if (LuceeApps.getDecisionUtil().isSimpleValue(luceeResult)) {
					// if Lucee returned a CFML simple-value then send it back in a message
					websocket.sendText(luceeResult.toString());
				}

				// System.out.println(LuceeEndpoint.class.getSimpleName() + ".onMessage");
			}
		});

		// System.out.println(this.getClass().getName() + " >>> exit onOpen()");
	}

	/**
	 * Event that is triggered when a session has closed.
	 *
	 * @param wsSession
	 *            The session
	 * @param closeReason
	 *            Why the session was closed
	 */
	public void onClose(Session wsSession, CloseReason closeReason) {

		ConnectionManager connMgr = ConnectionManager.getConnectionManager(wsSession);
		connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " enter onClose(); " + closeReason.toString());

		WebSocket websocket = WebsocketUtil.asWebsocketWrapper(wsSession);

		Object luceeResult = WebsocketUtil.invokeListenerMethodWithNamedArgs(websocket, LISTENER_METHOD_ON_CLOSE,
				ARG_WEBSOCKET, websocket, ARG_CLOSE_REASON, closeReason, ARG_SESSION_SCOPE, websocket.getSessionScope(),
				ARG_APPLICATION_SCOPE, websocket.getApplicationScope());

		connMgr.unsubscribeAll(WebsocketUtil.asWebsocketWrapper(wsSession));
	}

	/**
	 * Event that is triggered when a protocol error occurs.
	 *
	 * @param wsSession
	 *            The session.
	 * @param throwable
	 *            The exception.
	 */
	public void onError(Session wsSession, Throwable throwable) {

		ConnectionManager connMgr = ConnectionManager.getConnectionManager(wsSession);

		connMgr.log(Log.LEVEL_DEBUG,
				"connection " + wsSession.getId() + " enter onError(); " + WebsocketUtil.getStackTrace(throwable));

		WebSocket websocket = WebsocketUtil.asWebsocketWrapper(wsSession);

		Object luceeResult = WebsocketUtil.invokeListenerMethodWithNamedArgs(websocket, LISTENER_METHOD_ON_ERROR,
				ARG_WEBSOCKET, websocket, ARG_ERROR, throwable, ARG_SESSION_SCOPE, websocket.getSessionScope(),
				ARG_APPLICATION_SCOPE, websocket.getApplicationScope());

		connMgr.unsubscribeAll(WebsocketUtil.asWebsocketWrapper(wsSession));
	}

}
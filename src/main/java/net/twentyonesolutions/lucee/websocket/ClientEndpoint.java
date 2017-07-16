package net.twentyonesolutions.lucee.websocket;

import lucee.runtime.Component;
import net.twentyonesolutions.lucee.app.LuceeAppsUtil;

import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static net.twentyonesolutions.lucee.websocket.Constants.ARG_CLOSE_REASON;
import static net.twentyonesolutions.lucee.websocket.Constants.ARG_ERROR;
import static net.twentyonesolutions.lucee.websocket.Constants.ARG_MESSAGE;
import static net.twentyonesolutions.lucee.websocket.Constants.ARG_WEBSOCKET;
import static net.twentyonesolutions.lucee.websocket.Constants.LISTENER_METHOD_ON_CLOSE;
import static net.twentyonesolutions.lucee.websocket.Constants.LISTENER_METHOD_ON_ERROR;
import static net.twentyonesolutions.lucee.websocket.Constants.LISTENER_METHOD_ON_MESSAGE;
import static net.twentyonesolutions.lucee.websocket.Constants.LISTENER_METHOD_ON_OPEN;

public class ClientEndpoint extends Endpoint {

    Component listener;
    URI serverUri;

    public ClientEndpoint(Component listener, URI serverUri ) throws IOException, DeploymentException, URISyntaxException {

        this.listener = listener;
        this.serverUri = serverUri ;

        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        Session wsSession = webSocketContainer.connectToServer(this, serverUri);
    }

	/**
	 * Event that is triggered when a new session starts.
	 *
	 * @param wsSession The new javax.websocket.Session.
	 * @param config The configuration with which the Endpoint was
	 */
	@Override
	public void onOpen(Session wsSession, EndpointConfig config) {

		System.out.printf("%s.onOpen\n", this.getClass().getName());

		WebSocket websocket = WebsocketUtil.asWebsocketWrapper(wsSession);

		Object luceeResult = WebsocketUtil.invokeMethodWithNamedArgs(
				listener
				,LISTENER_METHOD_ON_OPEN
				,ARG_WEBSOCKET
				,websocket

				        /*
				,ARG_MESSAGE
				,message
				        ,ARG_SESSION_SCOPE
				        ,websocket.getSessionScope()
				        ,ARG_APPLICATION_SCOPE
				        ,websocket.getApplicationScope()
				        //*/
		);


		if (LuceeAppsUtil.isBooleanFalse(luceeResult)) {
			// terminate the connection if listener returned false

//			connMgr.log(Log.LEVEL_INFO, "connection " + wsId + " listener.onOpen() refused the connection by returning false. closing connection.");

			try {
				websocket.close();
			}
			catch (IOException e) {
			}
		}


		wsSession.addMessageHandler(new MessageHandler.Whole<String>() {

			public void onMessage(String message) {

				// connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " enter onMessage(); " + message);

				Object luceeResult = WebsocketUtil.invokeMethodWithNamedArgs(
				         listener
				        ,LISTENER_METHOD_ON_MESSAGE
				        ,ARG_WEBSOCKET
				        ,websocket
				        ,ARG_MESSAGE
				        ,message
				        /*
				        ,ARG_SESSION_SCOPE
				        ,websocket.getSessionScope()
				        ,ARG_APPLICATION_SCOPE
				        ,websocket.getApplicationScope()
				        //*/
                );

				// connMgr.log(Log.LEVEL_DEBUG, "listener.onMessage() "
				// + (luceeResult == null ? "did not return a value" : "returned: " + luceeResult.toString()));

				if (LuceeAppsUtil.getDecisionUtil().isSimpleValue(luceeResult)) {
					// if Lucee returned a CFML simple-value then send it back in a message
					websocket.sendText(luceeResult.toString());
				}

				// System.out.println(ServerEndpoint.class.getSimpleName() + ".onMessage");
			}
		});
	}


	/**
	 * Event that is triggered when a session has closed.
	 *
	 * @param wsSession     The session
	 * @param closeReason Why the session was closed
	 */
	@Override
	public void onClose(Session wsSession, CloseReason closeReason) {

//		connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " enter onClose(); " + closeReason.toString());

		Object luceeResult = WebsocketUtil.invokeMethodWithNamedArgs(
				 listener
				,LISTENER_METHOD_ON_CLOSE
				,ARG_WEBSOCKET
				,WebsocketUtil.asWebsocketWrapper(wsSession)
				,ARG_CLOSE_REASON
				,closeReason
			/*
				, ARG_SESSION_SCOPE, websocket.getSessionScope(),
				ARG_APPLICATION_SCOPE, websocket.getApplicationScope()
			//*/
		);
	}

	/**
	 * Event that is triggered when a protocol error occurs.
	 *
	 * @param wsSession   The session.
	 * @param throwable The exception.
	 */
	@Override
	public void onError(Session wsSession, Throwable throwable) {

//		connMgr.log(Log.LEVEL_DEBUG, "connection " + wsSession.getId() + " enter onError(); " + WebsocketUtil.getStackTrace(throwable));

		Object luceeResult = WebsocketUtil.invokeMethodWithNamedArgs(
				 listener
				,LISTENER_METHOD_ON_ERROR
				,ARG_WEBSOCKET
				,WebsocketUtil.asWebsocketWrapper(wsSession)
				,ARG_ERROR
				,throwable
			/*
				, ARG_SESSION_SCOPE, websocket.getSessionScope(),
				ARG_APPLICATION_SCOPE, websocket.getApplicationScope()
			//*/
		);
	}
}

package net.twentyonesolutions.lucee.websocket.functions;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import net.twentyonesolutions.lucee.websocket.ClientEndpoint;
import net.twentyonesolutions.lucee.websocket.WebSocket;

import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

/**
 * Created by Igal on 07/14/2017.
 */
public class WebsocketClient extends BIF {

    @Override
    public Object invoke(PageContext pageContext, Object[] objects) throws PageException {

        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();

        try {

            String uri = (String)objects[0];
            URI serverUri = new URI(uri);

            Component listener = (Component)objects[1];

            Endpoint clientEndpoint = new ClientEndpoint(listener, serverUri);

            Session wsSession = webSocketContainer.connectToServer(clientEndpoint, serverUri);

            WebSocket webSocket = new WebSocket(wsSession);

            return clientEndpoint;
        }
        catch (Throwable t){

            t.printStackTrace();
//            throw LuceeApps.toPageException(t);
        }

        return null;
    }

}

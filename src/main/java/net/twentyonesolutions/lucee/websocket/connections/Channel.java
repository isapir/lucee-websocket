package net.twentyonesolutions.lucee.websocket.connections;

import net.twentyonesolutions.lucee.websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Admin on 3/5/2017.
 */
class Channel {

    final String id;
    final ConcurrentHashMap<WebSocket, Boolean> map;


    Channel(String id){

        this.id = id;
        this.map = new ConcurrentHashMap<>();
    }


    public int getSubscriberCount(){

        return map.size();
    }


    public String getId(){

        return id;
    }


    public Set<WebSocket> getSubscribers(){

//        return map.keySet();
        Set<WebSocket> result = new HashSet(map.keySet());
        return result;
    }



    int subscribe(WebSocket connection){

        map.put(connection, true);

        connection.getChannels().add(id);

        return map.size();
    }


    int unsubscribe(WebSocket connection){

        map.remove(connection);

        connection.getChannels().remove(id);

        return map.size();
    }

}
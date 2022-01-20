# README for lucee websockets

# lucee-websocket: a Lucee WebSocket library

[lucee-websocket](https://github.com/isapir/lucee-websocket) is a server implementation of the [WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers) for [Lucee](https://lucee.org). 

>&nbsp;<br/>
>⚠️ **Breaking Change (v2.0.1 - 2017/07/14)**
>
>The function `WebsocketRegister()` has been renamed to `WebsocketServer()` since a `WebsocketClient()` function may be added in the future. See below for the most current instructions on installing.
>&nbsp;<br/><br/>


# Getting Started

## Requirements

- Java 8
- JSR-356 Compliant Servlet Container (e.g., Tomcat 8, Jetty 9.1)
- Lucee 5.1.3.18

## Installation

>&nbsp;<br/>
>💁🏼‍♀️ &nbsp; Watch the [Getting Started](https://www.youtube.com/watch?v=r2s2kGQVZqg) video.
>&nbsp;<br/><br/>

1. Install Lucee WebSockets Extension.
    1. The easiest way to install the extension on the server is via the Lucee Admin.
        1. Log into the Web or Server Admin and navigate to the ***Extension → Applications*** page (e.g., `https://[domain.name]/lucee/admin/server.cfm?action=ext.applications`).
        2. Click the ***Lucee Websockets Extension*** icon. On the next page, confirm the version to install and click the ***Install*** button.
    2. Alternatively, you can download the `.lex` file and drop it in the `deploy` directory of your Lucee setup.

2. Add the following snippet to your web deployment descriptor (`web.xml`).
    
    >⚠️&nbsp; If you are <b>NOT</b> using Tomcat as your servlet container, download the JAR file [servlet-filter-utils-1.1.1.jar](https://github.com/isapir/lucee-websocket/releases/download/2.0.3/servlet-filter-utils-1.1.1.jar) and save it to the classpath. If you’re using Jetty, it can go into `{jetty}/lib/ext`
  
    >ℹ️ &nbsp; Modify the `url-pattern` to match your URLs that will used with WebSockets.

    
    ```xml
    <!-- Required for the Lucee WebSocket Extension !-->
    
    <?xml version="1.0" encoding="ISO-8859-1"?>
    <!DOCTYPE web-app
         PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
        "http://java.sun.com/dtd/web-app_2_3.dtd">
    <web-app>
        <!-- Use this filter code block if installing on Tomcat !-->
        <filter>
            <filter-name>HttpSessionInitializerFilter</filter-name>
            <filter-class>org.apache.catalina.filters.SessionInitializerFilter</filter-class>
        </filter>

        <!-- Use this filter code block if you installed servlet-filter-utils-1.1.1.jar manually, e.g. for Jetty !-->
        <!-- 
        <filter>
            <filter-name>HttpSessionInitializerFilter</filter-name>
            <filter-class>net.twentyonesolutions.servlet.filter.HttpSessionInitializerFilter</filter-class>
        </filter> 
        !-->
    
    	<!-- modify url-pattern to match your websocket endpoints !-->
        <filter-mapping>
            <filter-name>HttpSessionInitializerFilter</filter-name>
            <url-pattern>/ws/*</url-pattern>
        </filter-mapping>
    </web-app>
    ```
    
3. Restart the Servlet Container (e.g., Tomcat, Jetty, etc.)
4. If you have a web server relaying requests to Lucee, follow the web server configuration instructions.

## Usage (Server)

>&nbsp;<br/>
>💁🏼‍♀️ &nbsp; Watch the [Chart Server Example](https://www.youtube.com/watch?v=rvB7PcNylVY) tutorial.
>&nbsp;<br/><br/>

1. Create a WebSocket listener. 
    
    * Listeners implement the event handling methods as specified in the Listener Component API. Examples of different types of Listeners can be found in the wiki.
    
    ```jsx
    component {
    
    		function onOpen(websocket, endpointConfig, sessionScope, applicationScope) {
    			// method is called when a connection from a client websocket is being opened
    			// accept the connection by returning true
    			// or reject the connection by returning false or throwing an exception.
    		}
    
        function onMessage(websocket, message, sessionScope, applicationScope){
    			// method is called when the client websocket sends a message
    			// return a String object to reply back to the websocket client
    
    			var message = "Echo from Lucee #Server.lucee.version# [#arguments.message#] @ #getTickCount()#";
          return message;
        }
    }
    ```
    
2. Configure the WebSocket endpoint on the server by specifying an endpoint and the listener created above.
   * The `endpoint` is the URI for the incoming WebSocket connections (e.g., `/ws/main` or `/ws/chat/{channel}`)
    
    ```jsx
        // WebsocketServer(String endpoint, Component listener :ConnectionManager

      endpoint = '/ws/echo';
      listener = new EchoListener();
      WebsocketServer(endpoint, listener);
    ```
    
    >&nbsp;<br/>
    >❗ Configuration of each endpoint should only be done once. You may want to do so in the ***Application.cfc*** `onApplicationStart()`. See Topics of Interest below.
    >&nbsp;<br/><br/>
    
    

# Testing
Testing can be done via any number of WebSocket clients. Instructions for testing via Javascript client (e.g., browser console) are noted here.

## **Javascript**

1. Create a WebSocket object using the endpoint for your listener.
    
    ```jsx
    var endpoint = "/ws/echo";
    var protocol = (document.location.protocol == "https:") ? "wss://" : "ws://";
    var url = protocol + document.location.host + endpoint;
    
    var wsecho = new WebSocket(url);
    ```
    
2. Use the WebSocket object to subscribe to messages using event handlers.
    
    ```jsx
    var log = function(evt){ console.log(evt); }
    wsecho.onopen    = log;
    wsecho.onmessage = log;
    wsecho.onerror   = log;
    wsecho.onclose   = log;
    ```
    
3. Send a message to the server.
    
    ```jsx
    if (wsecho.readState == WebSocket.OPEN){
        wsecho.send("Hello Lucee!");
    }
    ```
    

# Documentation

The [Project Wiki](https://github.com/isapir/lucee-websocket/wiki) contains the main documentation, including additional examples of listeners as well as the implemented [WebSocket API](https://github.com/isapir/lucee-websocket/wiki/WebSocket-API) and [Listener API](https://github.com/isapir/lucee-websocket/wiki/Listener-Component-API).

If you are having problems, check out the [Troubleshooting](https://github.com/isapir/lucee-websocket/wiki/Troubleshooting) section on the wiki for solutions to common errors, or post on the [Lucee Dev Discussion Forum](https://dev.lucee.org).

## Topics of interest

### Webserver configuration

- **Apache / Nginx config**
    - TODO link to wiki
- **IIS config**
    - TODO link to wiki
    
- **References**
    - [Troubleshooting Lucee Websockets with IIS and ARR - hacking / extensions - Lucee Dev](https://dev.lucee.org/t/troubleshooting-lucee-websockets-with-iis-and-arr/2687/3)
    - [Websockets reverse proxy in IIS 8 - Stack Overflow](https://stackoverflow.com/questions/34316825/websockets-reverse-proxy-in-iis-8)

### Starting WebSocket server via `onApplicationStart`

>&nbsp;<br/>
>💡 &nbsp;Note that a CFML page needs to be called for the application to start. Your WebSocket server will not be available until the application has started.
>&nbsp;<br/><br/>

- Check out the discussion at [https://github.com/isapir/lucee-websocket/issues/9#issuecomment-324705961](https://github.com/isapir/lucee-websocket/issues/9#issuecomment-324705961)

### Proxying / restarting WebSocket server

- Check out the discussion at [https://github.com/isapir/lucee-websocket/issues/9#issuecomment-772092393](https://github.com/isapir/lucee-websocket/issues/9#issuecomment-772092393)

## Known issues / Errors

- `The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method`
    - See [The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method · Issue #27 · isapir/lucee-websocket (github.com)](https://github.com/isapir/lucee-websocket/issues/27)
- `The WebSocket session [3] has been closed and no method (apart from close()) may be called on a closed session`
    - See [Connection mgr throws exception when broadcasting to closed connections · Issue #11 · isapir/lucee-websocket (github.com)](https://github.com/isapir/lucee-websocket/issues/11)
- `Multiple Endpoints may not be deployed to the same path`
    - See [Multiple Endpoints may not be deployed to the same path · Issue #9 · isapir/lucee-websocket (github.com)](https://github.com/isapir/lucee-websocket/issues/9)

# Contributing

Please see the contribution guidelines. You can also join the discussion at [Lucee WebSockets Extension on Lucee Dev](https://dev.lucee.org/t/lucee-websockets-extension/2067).

# Copyright / License

Copyright 2016-2022 Igal Sapir

This software is licensed under the Lesser GNU General Public License Version 2.1 (or later); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at: [http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
## Requirements

* Java 8
* JSR-356 Compliant Servlet Container, e.g. Tomcat 8, Jetty 9.1, etc.
* Lucee 5.1.3.18

## Installation 

The easiest way to install the extension is from the Lucee Admin.  Navigate to the Extension Applications page in the Web or Server Admin, e.g. `/lucee/admin/server.cfm?action=ext.applications`.  Click the *Lucee Websockets Extension* icon, and then on the next page click the `Install` button.

## Getting Started

The WebSocket API is event driven, meaning that you register event handling methods (e.g. _onOpen()_, _onMessage()_, etc), and those are called when the corresponding events are triggered.

To configure a WebSocket endpoint call the function 

    WebsocketRegister(String endpoint, Component listener):ConnectionManager
    
Where `endpoint` is the URI for the incoming WebSocket connections, e.g. `/ws/chat/{channel}`, and the Component listener is a component that implements any of the event handling methods as specified in the [Listener Component API](../../wiki/Listener-Component-API).
    
This should only be done once, so you can do that in Application.cfc's onApplicationStart().

Each _endpoint_ has its own [ConnectionManager](../../wiki/ConnectionManager-API) object, which keeps track of all of the client WebSockets that are connected to that _endpoint_.  You can either store the _ConnectionManager_ in an Application scope variable, or retrieve it from a [WebSocket](wiki/WebSocket-API) object that is passed as as argument to some of the event handlers by calling the method _getConnectionManager()_ on that argument, i.e. `arguments.websocket.getConnectionManager()`.



## Copyright / License

Copyright 2016-2017 Igal Sapir

This software is licensed under the Lesser GNU General Public License Version 2.1 (or later); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
[http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


/*
 * Copyright (C) 2011 The Code Bakers
 * Authors: Cleuton Sampaio e Francisco Rodrigues
 * e-mail: thecodebakers@gmail.com
 * Project: http://code.google.com/p/netbaker-android
 * Site: http://www.thecodebakers.org
 *
 * Licensed under the GNU LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * The over the LGPL V 3 there is the following addition: 
 * - The authors may create another version of the program which will be distributed
 *   as proprietary software.
 * 
 * @author Cleuton Sampaio e Francisco Rogrigues - thecodebakers@gmail.com
 */
package org.thecodebakers.webxpose.netbaker.core;

import java.net.Socket;
/**
 * Plugable protocol interface.
 * @author The Code Bakers
 *
 */
public interface InetBakerProtocol {
	/**
	 * Inform Service instance.
	 * @param service Service instance.
	 */
	void setServiceInstance(NetBakerService service);
	/**
	 * Process a request.
	 * @param s Client socket.
	 * @return true - to kill the protocol and the server. false - ok, continue.
	 */
	boolean processRequest(Socket s);
}

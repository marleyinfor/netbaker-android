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

package org.thecodebakers.webxpose.netbaker.protocols;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;

import org.thecodebakers.webxpose.netbaker.core.InetBakerProtocol;
import org.thecodebakers.webxpose.netbaker.core.NetBakerService;
import org.thecodebakers.webxpose.netbaker.core.NetBakerService.MSGTYPE;

//This is a very simple HTTP protocol implementation to demonstrate NetBaker
//It returns the date and time upon each request
//Implementação simples de protocolo HTTP, apenas para demonstração
//Retorna a data e hora a cada request

/**
 * A sample InetBakerProtocol implementation class.
 */
public class NetBakerHttp implements InetBakerProtocol {
	
	private NetBakerService service;

	/**
	 * Reveive a service instance
	 */
	public void setServiceInstance(NetBakerService service) {
		this.service = service;
	}

	/**
	 * process a request. Socket "s" was accepted by the service.
	 */
	public boolean processRequest(Socket s) {
		/*
		 * Use the same messaging mechanism of the service.
		 * Create your own message file and number it above 100
		 * To be consistent with NetBaker's architecture, always call 
		 * NetBakerService.msg() function.
		 */
		
		// Return true to stop service!
		
		boolean resultado = false;
		
		try {
			this.service.msg(MSGTYPE.TDEBUG, this.service.getProps("NBH_enderedProcessing", 105)
					+ " Thread ID " + Thread.currentThread().getId()
					+ " Socket: " + s.toString());
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
					s.getInputStream()));
			// Read request
			String linha = br.readLine();
			this.service.msg(MSGTYPE.TINFO, this.service.getProps("NBH_received", 101) + linha);
		
			StringBuffer saida = new StringBuffer();
			Date data = new Date();
			String mensagem = "<html><body><h3>Date " + data + "</h3></body></html>";
			saida.append("HTTP/1.0 200 OK\r\n");
			saida.append("Server: NetBaker HTTP demo server\r\n");
			saida.append("Content-Type: text/html\r\n");
			saida.append("Content-length: " + mensagem.getBytes().length + "\r\n");
			saida.append("Connection: close\r\n");
			saida.append("\r\n");
			saida.append(mensagem);
			
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			for (long inx = 0; inx < saida.toString().getBytes().length; inx++) {
				dos.write(saida.toString().getBytes()[(int) inx]); 
			}
			dos.flush();
			dos.close();
		}
		catch(IOException ioe) {
			this.service.msg(MSGTYPE.TERROR, this.service.getProps("NBH_iOException", 102) + " ## THREAD: "  + Thread.currentThread().getId());
		}
		catch(Exception ex) {
			this.service.msg(MSGTYPE.TERROR, this.service.getProps("NBH_exception", 103) + ex.getMessage() + " ## THREAD: "  + Thread.currentThread().getId());
		}
		finally {
			if (s != null) {
				if (!s.isClosed()) {
					try {
						s.close();
					} catch (IOException e) {
						this.service.msg(MSGTYPE.TERROR, this.service.getProps("NBH_exceptionClosingSocket", 104) + " " + e.getMessage() + " ## THREAD: "  + Thread.currentThread().getId());
					}
				}
			}
		}
		return resultado;
	}

}

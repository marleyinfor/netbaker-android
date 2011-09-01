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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
/**
 * This class is an Android Service which receives socket connections.
 * @author The Code Bakers
 *
 */
public class NetBakerService extends Service {

	protected Resources res;
	protected int porta;
	protected int portaAdm;
	protected ServerSocket serverSocket;
	protected ServerSocket serverAdmSocket;
	protected Thread mainProcessThread;
	protected Thread adminProcessThread;
	protected final String TAG = "NetBaker";
	public String serverName;
	protected static NetBakerService selfRef;
	/**
	 * debug True - gerenate debug info in log
	 */
	public boolean debug = true;
	/**
	 * verbose True - gerenate info messages in log
	 */
	public boolean verbose = true;
	protected String protocolClassName = null;
	protected String packageName;

	// Constants
	/**
	 * Message types
	 */
	public static enum MSGTYPE {TINFO, TDEBUG, TWARN, TERROR};
	
	// Main method
	@Override
	/**
	 * Called when the service is about to be started.
	 */
	public int onStartCommand(Intent intent, int flags, int startId) {
		res = this.getResources();
		NetBakerService.selfRef = this;
		parseExtras(intent);
		this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " extras: " + intent.getExtras().size());
		this.msg(MSGTYPE.TINFO, this.getProps("NB_servidorIniciando", 1));

		try {
			serverSocket = new ServerSocket(porta);
			this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " Server Socket on port: " + porta);
			try {
				serverAdmSocket = new ServerSocket(portaAdm);
				this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " Admin Socket on port: " + portaAdm);
				ProcessRequests procReq = new ProcessRequests(serverSocket);
				mainProcessThread = new Thread(procReq);
				mainProcessThread.start();
				ProcessAdminRequests procAdm = new ProcessAdminRequests(serverAdmSocket);
				adminProcessThread = new Thread(procAdm);
				adminProcessThread.start();
				// Is good when everything runs ok...
				this.msg(MSGTYPE.TINFO, this.getProps("NB_onstartok", 2));
				return START_STICKY;
			}
			catch (Exception ex) {
				this.msg(MSGTYPE.TERROR, this.getProps("NB_errorOpeningAdminPort", 3));
			}
		}
		catch (Exception ex) {
			this.msg(MSGTYPE.TERROR, this.getProps("NB_errorOpeningPort", 4));
		}
		return START_NOT_STICKY;
	}
	
	// Main request processing thread
	class ProcessRequests implements Runnable {
		
		private ServerSocket srvSock;
		
		ProcessRequests(ServerSocket ss) {
			this.srvSock = ss;
		}

		public void run() {
			
			// This class instantiates a thread to process each request
			
			Thread clientThread;
			while (true) {
				try {

					if (Thread.interrupted()) {
					    throw new InterruptedException();
					}
					if (srvSock == null || srvSock.isClosed()) {
						throw new InterruptedException();
					}
					Socket s = srvSock.accept();
					InetBakerProtocol protocol = (InetBakerProtocol) Class.forName(NetBakerService.selfRef.protocolClassName).newInstance();
					protocol.setServiceInstance(NetBakerService.selfRef);
					ProtocolProcessing protocolProcessing = new ProtocolProcessing(protocol,s);
					clientThread = new Thread(protocolProcessing);
					clientThread.start();
					protocol = null;
				}
				catch(IOException ioe) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_ioExceptionProcessinProtocol", 17));
				}
				catch(InterruptedException ex) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_serverSocketIsClosed", 21));
					break;
				}
				catch(Exception ex) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_exceptionProcessinProtocol", 18) + " " + ex.getMessage());
				}
			}			
		}
	}
	
	// Worker thread class
	
	class ProtocolProcessing implements Runnable {

		private InetBakerProtocol protocol;
		private Socket s;
		
		ProtocolProcessing (InetBakerProtocol protocol, Socket s) {
			this.protocol = protocol;
			this.s = s;
		}
		
		public void run() {
			// If protocol returns true, then we must stop the service
			if (protocol.processRequest(s)) 	{
				NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_terminateByRequest", 19));
				NetBakerService.selfRef.stopEverything();
			}
				
		}
	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
	
	
	
	// Main request admin processing thread
	class ProcessAdminRequests implements Runnable {
		
		private ServerSocket srvSock;
		
		ProcessAdminRequests(ServerSocket ss) {
			this.srvSock = ss;
		}

		public void run() {
			while (true) {
				Socket s = null;
				try {
                    s = srvSock.accept();
                    if(processa(s)) {
                    	// Request to stop received:
                        s.close();	// Close socket anyway
                        s = null;
                    	NetBakerService.selfRef.stopEverything();
                    	NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_stoppingService", 12));
                    	return;
                    }
                    s.close();	// Close socket anyway
                    s = null;
				}
				catch(IOException ioe) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_exceptionAdminRequest", 15));
					break;
				}
			}							
		}
		
		private boolean processa(Socket s) {
			boolean resultado = false;
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(
						s.getInputStream()));
				
				// Read what came into the request
				String linha = br.readLine();
				NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_adminRequest", 13) + " : " + linha);

				if (linha.indexOf("shutdown.cgi") >= 0) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_requestShutDown", 14));
					NetBakerService.selfRef.stopSelf();
					resultado = true;
				}
			}
			catch (IOException ioe) {
				resultado = true;
				NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_exceptionReadAdminRequest", 16));
			}
			return resultado;
		}
		
	}
	
	// Utility
	/**
	 * Stop all threads and server sockets
	 */
	public synchronized void stopEverything() {
		try {
			this.mainProcessThread.interrupt();
			this.adminProcessThread.interrupt();
			this.serverSocket.close();
			this.serverAdmSocket.close();
		} catch (IOException e) {
			this.msg(MSGTYPE.TERROR, this.getProps("NB_exceptionStopping", 11) + " " + e.getMessage());
		}
		
	}
	
	private boolean parseExtras(Intent intent) {
		boolean resultado = true;
		
		Bundle extras = intent.getExtras();
		this.packageName = extras.getString("packageName");
		try {
			this.porta = extras.getInt("port");
			this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " port: " + this.porta);
			this.portaAdm = extras.getInt("adminPort");
			this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " adminport: " + this.portaAdm);
		}
		catch (NumberFormatException nfe) {
			this.msg(MSGTYPE.TERROR, this.getProps("NB_nonNumericPorts", 5));
			resultado = false;
		}
		this.serverName = extras.getString("serverName");
		this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " serverName: " + this.serverName);
		this.debug = extras.getBoolean("debug");
		this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " debug: " + this.debug);
		this.verbose = extras.getBoolean("verbose");
		this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " verbose: " + this.verbose);
		this.protocolClassName = extras.getString("protocolClassName");
		this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " protocolClassName: " + this.protocolClassName);
		if (this.protocolClassName == null) {
			this.msg(MSGTYPE.TERROR, this.getProps("NB_missingProtocolClassName", 6));
			resultado = false;
		}
		else {
			try {
				Class.forName(this.protocolClassName);
				try {
					InetBakerProtocol prot = (InetBakerProtocol) Class.forName(this.protocolClassName).newInstance();
					this.msg(MSGTYPE.TDEBUG, this.getProps("NB_debug", 20) + " protocol class successfully loaded.");
					prot = null;
				}
				catch (ClassCastException cce) {
					this.msg(MSGTYPE.TERROR, this.getProps("NB_cannotCastProtocol", 10));
					resultado = false;
				} catch (IllegalAccessException e) {
					this.msg(MSGTYPE.TERROR, this.getProps("NB_cannotAccessProtocol", 8));
					resultado = false;
				} catch (InstantiationException e) {
					this.msg(MSGTYPE.TERROR, this.getProps("NB_cannotInstantiateProtocol", 9));
					resultado = false;
				}
			}
			catch (ClassNotFoundException cnf) {
				this.msg(MSGTYPE.TERROR, this.getProps("NB_missingProtocolClass", 7));
				resultado = false;
			}
		}
		return resultado;
	}
	
	/**
	 * Write a message to the log
	 * @param tipo Message type TINFO, TDEBUG, TERROR, TWARN
	 * @param mensagem Message text
	 */
	public void msg(MSGTYPE tipo, String mensagem) {
		switch(tipo) {
		case TINFO:
			if (this.verbose) {
				Log.i(this.TAG, mensagem);
			}
			break;
		case TWARN:
			if (this.verbose) {
				Log.w(this.TAG, mensagem);
			}
			break;
		case TDEBUG:
			if (this.debug) {
				Log.d(this.TAG, mensagem);
			}
			break;
		case TERROR:
			Log.e(this.TAG, mensagem);
		}
	}
	
	/**
	 * Try to get a string resource. Otherwise, show the message number.
	 * @param nomeString String resource name
	 * @param sit Message number to show
	 * @return Message text
	 */
	public String getProps(String nomeString, int sit) {
		String retorno = null;
		try {
			int id = res.getIdentifier(nomeString, "string", this.packageName);
			retorno = res.getString(id);
		}
		catch(Resources.NotFoundException rnf) {
			retorno = "NB message situation: " + sit;
		}
		return retorno;
	}
	

	// Other
	
	
}

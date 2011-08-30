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
	public boolean debug = true;
	public boolean verbose = true;
	public boolean toastError = false;
	protected String protocolClassName = null;
	protected InetBakerProtocol protocol = null;
	protected String packageName;

	// Constants
	public static enum MSGTYPE {TINFO, TDEBUG, TWARN, TERROR};
	
	// Main method
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		res = this.getResources();
		if (this.serverName.length() == 0) {
			serverName = TAG;
		}
		this.msg(MSGTYPE.TINFO, this.getProps("NB_servidorIniciando", 1));
		parseExtras(intent);

		try {
			serverSocket = new ServerSocket(porta);
			this.packageName = getPackageName();
			try {
				serverAdmSocket = new ServerSocket(portaAdm);
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
					Socket s = srvSock.accept(); 
					NetBakerService.selfRef.protocol.setSocket(s);
					ProtocolProcessing protocolProcessing = new ProtocolProcessing(); 
					clientThread = new Thread(protocolProcessing);
					clientThread.start();
				}
				catch(IOException ioe) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_ioExceptionProcessinProtocol", 17));
				}
				catch(InterruptedException ex) {
					break;
				}
				catch(Exception ex) {
					NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_exceptionProcessinProtocol", 18));
				}
			}			
		}
	}
	
	// Worker thread class
	
	class ProtocolProcessing implements Runnable {
		public void run() {
			
			// If protocol returns true, then we must stop the service
			
			if (NetBakerService.selfRef.protocol.processRequest()) 	{
				NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_terminateByRequest", 19));
				NetBakerService.selfRef.stopEverything();
			}
		}
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
                    	NetBakerService.selfRef.stopEverything();
                    	NetBakerService.selfRef.msg(MSGTYPE.TINFO, NetBakerService.selfRef.getProps("NB_stoppingService", 12));
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
		try {
			this.porta = Integer.parseInt(extras.getString("port"));
			this.portaAdm = Integer.parseInt(extras.getString("adminPort"));
		}
		catch (NumberFormatException nfe) {
			this.msg(MSGTYPE.TERROR, this.getProps("NB_nonNumericPorts", 5));
			resultado = false;
		}
		this.serverName = extras.getString("serverName");
		this.debug = extras.getBoolean("debug");
		this.verbose = extras.getBoolean("verbose");
		this.toastError = extras.getBoolean("toastError");
		this.protocolClassName = extras.getString("protocolClassName");
		if (this.protocolClassName == null) {
			this.msg(MSGTYPE.TERROR, this.getProps("NB_missingProtocolClassName", 6));
			resultado = false;
		}
		else {
			try {
				Class.forName(this.protocolClassName);
				try {
					this.protocol = (InetBakerProtocol) Class.forName(this.protocolClassName).newInstance();
					this.protocol.setServiceInstance(this);
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
	
	public void msg(MSGTYPE tipo, String mensagem) {
		switch(tipo) {
		case TINFO:
			if (this.verbose) {
				Log.i(this.TAG, mensagem);
			}
		case TWARN:
			if (this.verbose) {
				Log.w(this.TAG, mensagem);
			}
		case TDEBUG:
			if (this.debug) {
				Log.d(this.TAG, mensagem);
			}
		case TERROR:
			Log.e(this.TAG, mensagem);
			if (this.toastError) {
				Toast.makeText(this.getApplicationContext(), mensagem, Toast.LENGTH_LONG);
			}
		}
	}
	
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
	
	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	
}

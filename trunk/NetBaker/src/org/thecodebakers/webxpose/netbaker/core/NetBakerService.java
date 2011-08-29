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

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.thecodebakers.webxpose.netbaker.R;
import org.thecodebakers.webxpose.netbaker.core.NetBakerServerService.ProcessAdminRequests;
import org.thecodebakers.webxpose.netbaker.core.NetBakerServerService.ProcessWebRequests;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
	protected String serverName;
	protected static NetBakerService selfRef;
	protected boolean debug = true;
	protected boolean verbose = true;
	protected boolean toastError = false;
	protected String protocolClassName = null;
	protected InetBakerProtocol protocol = null;

	// Constants
	protected static enum MSGTYPE {TINFO, TDEBUG, TWARN, TERROR};
	
	// Main method
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		res = this.getResources();
		if (this.serverName.length() == 0) {
			serverName = TAG;
		}
		this.msg(MSGTYPE.TINFO, this.getProps(R.string.servidorIniciando, 1));
		parseExtras(intent);

		try {
			serverSocket = new ServerSocket(porta);
			try {
				serverAdmSocket = new ServerSocket(portaAdm);
				ProcessWebRequests procReq = new ProcessWebRequests(serverSocket);
				mainProcessThread = new Thread(procReq);
				mainProcessThread.start();
				ProcessAdminRequests procAdm = new ProcessAdminRequests(serverAdmSocket);
				adminProcessThread = new Thread(procAdm);
				adminProcessThread.start();
				// Is good when everything runs ok...
				this.msg(MSGTYPE.TINFO, this.getProps(R.string.onstartok, 2));
				return START_STICKY;
			}
			catch (Exception ex) {
				this.msg(MSGTYPE.TERROR, this.getProps(R.string.errorOpeningAdminPort, 3));
			}
		}
		catch (Exception ex) {
			this.msg(MSGTYPE.TERROR, this.getProps(R.string.errorOpeningPort, 4));
		}
		return START_NOT_STICKY;
	}
	
	// Main request processing thread
	
	// Main request admin processing thread

	// Utility
	
	private boolean parseExtras(Intent intent) {
		boolean resultado = true;
		Bundle extras = intent.getExtras();
		try {
			this.porta = Integer.parseInt(extras.getString("port"));
			this.portaAdm = Integer.parseInt(extras.getString("adminPort"));
		}
		catch (NumberFormatException nfe) {
			this.msg(MSGTYPE.TERROR, this.getProps(R.string.nonNumericPorts, 5));
			resultado = false;
		}
		this.serverName = extras.getString("serverName");
		this.debug = extras.getBoolean("debug");
		this.verbose = extras.getBoolean("verbose");
		this.toastError = extras.getBoolean("toastError");
		this.protocolClassName = extras.getString("protocolClassName");
		if (this.protocolClassName == null) {
			this.msg(MSGTYPE.TERROR, this.getProps(R.string.missingProtocolClassName, 6));
			resultado = false;
		}
		else {
			try {
				Class.forName(this.protocolClassName);
				try {
					this.protocol = (InetBakerProtocol) Class.forName(this.protocolClassName).newInstance();
				}
				catch (ClassCastException cce) {
					this.msg(MSGTYPE.TERROR, this.getProps(R.string.cannotCastProtocol, 10));
					resultado = false;
				} catch (IllegalAccessException e) {
					this.msg(MSGTYPE.TERROR, this.getProps(R.string.cannotAccessProtocol, 8));
					resultado = false;
				} catch (InstantiationException e) {
					this.msg(MSGTYPE.TERROR, this.getProps(R.string.cannotInstantiateProtocol, 9));
					resultado = false;
				}
			}
			catch (ClassNotFoundException cnf) {
				this.msg(MSGTYPE.TERROR, this.getProps(R.string.missingProtocolClass, 7));
				resultado = false;
			}
		}
		return resultado;
	}
	
	protected void msg(MSGTYPE tipo, String mensagem) {
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
	
	protected String getProps(int id, int sit) {
		String retorno = null;
		try {
			retorno = res.getString(id);
		}
		catch(Resources.NotFoundException rnf) {
			retorno = "message " + sit;
		}
		return retorno;
	}
	

	// Other
	
	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	
}

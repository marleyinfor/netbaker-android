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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.thecodebakers.webxpose.netbaker.core.NetBakerService.MSGTYPE;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Convenience class to start NetBaker Service.
 * @author The Code Bakers
 *
 */
public class NetBakerStarter {
	public static Context context;
	public static boolean verbose = true;
	public static boolean debug = true;
	public static boolean toastError = true;
	
	private static final String ThisTag = "NetBakerStarter";
	
	/**
	 * Check network connectitivy.
	 * @return A list of IP Addresses available or an empty list. 
	 */
    public static List<String> checkNetwork() {
        List<String> enderecos = new ArrayList<String>();
        ConnectivityManager conMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conMgr.getActiveNetworkInfo();
        String ipa = "";
        if (info != null) {
                if (info.isAvailable()) {
                        if (info.isConnected()) {
                                try {
                                        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                                        while (nis.hasMoreElements()) {
                                                NetworkInterface ni = nis.nextElement();
                                                Enumeration<InetAddress> ipads = ni.getInetAddresses();
                                                while (ipads.hasMoreElements()) {
                                                        InetAddress ipad = ipads.nextElement();
                                                        ipa = "";
                                                        if (!ipad.isLoopbackAddress()) {
                                                                ipa += ipad.getHostAddress().toString();
                                                                if (ipad.isSiteLocalAddress()) {
                                                                        ipa += " (LOCAL)";
                                                                }
                                                        }
                                                }
                                                if (ipa.length() > 0) {
                                                        enderecos.add(ipa);
                                                }
                                        }
                                } catch (SocketException ex) {
                                		NetBakerStarter.msg(MSGTYPE.TERROR, NetBakerStarter.getProps("NBS_socketException", 50) + " " + ex.getMessage());
                                }
                        }
                        else {
                        		NetBakerStarter.msg(MSGTYPE.TERROR, NetBakerStarter.getProps("NBS_actNotConnected", 51));
                        }
                }
                else {
                		NetBakerStarter.msg(MSGTYPE.TERROR, NetBakerStarter.getProps("NBS_actConNotAvailable", 52));
                }               
        }
        else {
        		NetBakerStarter.msg(MSGTYPE.TERROR, NetBakerStarter.getProps("NBS_actConNotAvailableNull", 53));
        }
        return enderecos;
    }
    
    /**
     * Verify if the service is running by making a fake request to the admin port.
     * @param adminPort Admin port number
     * @return true - The service is running. False - the service is not running.
     */
    public static boolean checkAdminPort(int adminPort) {
        StringBuffer bigBuf = new StringBuffer();
        boolean resultado = false;
        try {
                InputStream strm = new URL("http://localhost:" + adminPort + "/").openStream();
                BufferedReader rdr = new BufferedReader(new InputStreamReader(strm));
                String thisLine = rdr.readLine();
                while (thisLine != null) {
                        bigBuf = bigBuf.append(thisLine);
                        thisLine = rdr.readLine();
                }
                rdr.close();
                resultado = true;
                } catch (Exception e) {
                }
        return resultado;       
    }
    
    /**
     * Start NetBaker Service.
     * @param porta Main port, used to process requests.
     * @param portaAdmin Service port, used to verify or kill the service.
     * @param serverName A name which can be used by the server.
     * @param protocolClassName Package and class name for the protocol class.
     */
    public static void startService(
    	int porta, 
    	int portaAdmin,
    	String serverName,
    	String protocolClassName) {
        Intent intent = new Intent(context, NetBakerService.class);
        intent.putExtra("packageName", context.getPackageName());
        intent.putExtra("serverName", serverName);
        intent.putExtra("port", porta);
        intent.putExtra("adminPort", portaAdmin);
        intent.putExtra("debug", NetBakerStarter.debug);
        intent.putExtra("verbose", NetBakerStarter.verbose);
        intent.putExtra("protocolClassName", protocolClassName);
        context.startService(intent);   
    }
    
    /**
     * Send an admin request to stop the server. 
     * @param adminPort Admin port.
     */
    public static void requestStop(int adminPort) {
        StringBuffer bigBuf = new StringBuffer();
        try {
                InputStream strm = new URL("http://localhost:" + adminPort + "/shutdown.cgi").openStream();
                BufferedReader rdr = new BufferedReader(new InputStreamReader(strm));
                String thisLine = rdr.readLine();
                while (thisLine != null) {
                        bigBuf = bigBuf.append(thisLine);
                        thisLine = rdr.readLine();
                }
                rdr.close();
                } catch (Exception e) {
                }
    }
    
    // Private methods
    
    
	private static void msg(MSGTYPE tipo, String mensagem) {
		switch(tipo) {
		case TINFO:
			if (NetBakerStarter.verbose) {
				Log.i(NetBakerStarter.ThisTag, mensagem);
			}
		case TWARN:
			if (NetBakerStarter.verbose) {
				Log.w(NetBakerStarter.ThisTag, mensagem);
			}
		case TDEBUG:
			if (NetBakerStarter.debug) {
				Log.d(NetBakerStarter.ThisTag, mensagem);
			}
		case TERROR:
			Log.e(NetBakerStarter.ThisTag, mensagem);
			if (NetBakerStarter.toastError) {
				Toast.makeText(NetBakerStarter.context, mensagem, Toast.LENGTH_LONG);
			}
		}
	}
	
	private static String getProps(String nomeString, int sit) {
		String retorno = null;
		try {
			int id = context.getResources().getIdentifier(nomeString, "string", context.getPackageName());
			retorno = context.getResources().getString(id);
		}
		catch(Resources.NotFoundException rnf) {
			retorno = "NB message situation: " + sit;
		}
		return retorno;
	}

}

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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetBakerStarter {
	public static Context context;
	private static final String ThisTag = "NetBakerStarter";
    public static List<String> checkNetwork() {
        Resources res = context.getResources();
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
                                        Log.e(ThisTag, ex.toString());
                                        Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
                                }
                        }
                        else {
                                Log.e(ThisTag, res.getString(R.string.actConNotConnected));
                                Toast.makeText(context, res.getString(R.string.actConNotConnected), Toast.LENGTH_LONG).show();
                        }
                }
                else {
                        Log.e(ThisTag, res.getString(R.string.actConNotAvailable));
                        Toast.makeText(context, res.getString(R.string.actConNotAvailable), Toast.LENGTH_LONG).show();
                }               
        }
        else {
                Log.e(ThisTag, res.getString(R.string.actConNotAvailableNull));
                Toast.makeText(context, res.getString(R.string.actConNotAvailableNull), Toast.LENGTH_LONG).show();
        }

    }

}

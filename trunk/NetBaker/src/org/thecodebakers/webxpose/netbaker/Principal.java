package org.thecodebakers.webxpose.netbaker;

import org.thecodebakers.webxpose.netbaker.core.NetBakerStarter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Principal extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void start(View view) {
    	NetBakerStarter.context = this.getApplicationContext();
    	NetBakerStarter.debug = true;
    	NetBakerStarter.toastError = true;
    	if (NetBakerStarter.checkNetwork().size() > 0) {
    		if (!NetBakerStarter.checkAdminPort(8081)) {
    			NetBakerStarter.startService(8080, 8081, "Test", "org.thecodebakers.webxpose.netbaker.protocols.NetBakerHttp");
    		}
    	}
    }
    
    public void stop(View view) {
    	NetBakerStarter.context = this.getApplicationContext();
    	NetBakerStarter.debug = true;
    	NetBakerStarter.toastError = true;
    	if (NetBakerStarter.checkNetwork().size() > 0) {
    		if (NetBakerStarter.checkAdminPort(8081)) {
    			NetBakerStarter.requestStop(8081);
    		}
    	}
    }
}
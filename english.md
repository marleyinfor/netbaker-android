# Introduction #

NetBaker is a TCP framework for Android Platform. You can create TCP server applications quickly by using these classes.


# Details #

NetBaker comes with a simple HTTP server as example, which you can modify. Or you can create a new server by implementing the InetBakerProtocol interface.

# Installation #

Please READ the license file "license.txt", located inside the project.

  1. Copy the package "org.thecodebakers.webxpose.netbaker.core" to your project's source folder;
  1. Create your own implementation of "InetBakerProtocol" interface. You must implement both methods. See comments inside our sample for instructions;
  1. Add network permissions to your "AndroidManifest.xml" file, as shown in the sample;
  1. Copy the string files "netbaker\_core\_messages.xml" and "netbaker\_starter\_messages.xml" to your project's "res/values" folder;
  1. Use class "NetBakerStarter" to check our start your server;

This is a basic code to start a NetBaker based server:
```
    	NetBakerStarter.context = this.getApplicationContext();
    	NetBakerStarter.debug = true;
    	NetBakerStarter.toastError = true;
    	if (NetBakerStarter.checkNetwork().size() > 0) {
    		if (!NetBakerStarter.checkAdminPort(8081)) {
    			NetBakerStarter.startService(8080, 8081, "Test", "org.thecodebakers.webxpose.netbaker.protocols.NetBakerHttp");
    		}
    	}
```

Where:
  * 8080 - The main port number. Must be above 1024;
  * 8081 - The admin port number. Must be above 1024 and different from main port number;
  * "Test" - The server name;
  * "org.thecodebakers.webxpose.netbaker.protocols.NetBakerHttp" - our sample implementation of an HTTP server. You must provide your own class;

This is a basic code to stop a NetBaker based server:
```
    	NetBakerStarter.context = this.getApplicationContext();
    	NetBakerStarter.debug = true;
    	NetBakerStarter.toastError = true;
    	if (NetBakerStarter.checkNetwork().size() > 0) {
    		if (NetBakerStarter.checkAdminPort(8081)) {
    			NetBakerStarter.requestStop(8081);
    		}
    	}
```

The only components you need to create are:
  * The starting activity of your application, which invokes the NetBakerStarter methods;
  * Your server's implementation class, which must implement InetBakerProtocol interface;
  * Please use message situation above 100;

NetBaker uses some standard strings, which can be localized if you want. To be consistent, I created a "situation" number, which is related to the situation and the message string. If NetBaker cannot find the string resource in your project's package, then it shows the number. If you create your own protocol, please use situation numbers above 100.
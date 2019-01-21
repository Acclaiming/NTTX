package io.kurumi.ntbot.auth;

import org.tio.http.server.*;
import org.tio.http.common.*;
import java.io.*;

public class AuthServer {

    private AuthManager manager;
	private HttpServerStarter server;
	private HttpConfig config;
	
	public AuthServer(AuthManager manager,int port) {
		
        this.manager = manager;
		config = new HttpConfig(port,null,null,null);
		config.setBindIp("127.0.0.1");
		server = new HttpServerStarter(config,new AuthRequestHanlder(manager));
		
	}
	
	public void start() throws IOException {
		
		server.start();
		
	}
	
	public void stop() throws IOException {
		
		server.stop();
		
	}
	
}

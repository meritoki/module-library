package com.meritoki.library.system.module;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.meritoki.module.library.model.Client;
import com.meritoki.module.library.model.Server;

/**
 * Unit test for simple App.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientServerTest 
{
    static Client client = new Client(0);
    static Server server = new Server(1);
    
    @BeforeAll
	public static void initialize() {
		client.start();
		server.start();
	}

	@Test
	@Order(1) 
	public void clientToServer() {
		
	}
	
	@Test
	@Order(2) 
	public void serverToClient() {
		
	}
	
	@Test
	@Order(3) 
	public void destroy() {
		
	}
}

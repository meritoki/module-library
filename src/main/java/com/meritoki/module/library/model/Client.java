package com.meritoki.module.library.model;

import java.util.concurrent.CountDownLatch;

public class Client extends Web {
	
	public static void main(String[] args) {
		Client client = new Client(0);
		CountDownLatch countDownLatch;
		client.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		client.start();
		while(client.getState() != client.INPUT) {
			System.out.println("Waiting...");
		}
		Protocol protocol = new Protocol();
		protocol.serialize(Protocol.MESSAGE,client.protocol.getMessageOffset(),client.protocol.getMessageAcknowledged(), "{Hello World}");
		
		client.add(new Data(0,0,Data.OUTPUT,0,protocol,null));
	}
	
	public Client(int id) {
		super(id);
	}

}

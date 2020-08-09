package com.meritoki.module.library.model;

import java.util.concurrent.CountDownLatch;

public class Server extends Web {

	
	public static void main(String[] args) {
		Server server = new Server(1);
		CountDownLatch countDownLatch;
		server.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		server.start();
	}
	
	
	public Server(int id) {
		super(id);
	}
}

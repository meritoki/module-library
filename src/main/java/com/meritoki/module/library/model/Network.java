/*
Copyright 2018 Josvaldor

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.meritoki.module.library.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import javax.management.MBeanServer;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.StringUtils;

public class Network extends Node {
	
	public static final int CONNECTION = 1;
	protected Socket socket = null;
	protected SSLSocket sslSocket = null;
	protected ServerSocket serverSocket = null;
	protected SSLServerSocket sslServerSocket = null;
	protected String hostAddress = null;
	protected int port = -1;
	protected String path = null;
	protected String httpURL = null;
	protected String javaxNETSSLKeyStorePassword;
	protected String javaxNETSSLKeyStorePath;
	protected String javaxNETSSLTrustStorePath;
	protected String javaxNETSSLTrustStorePassword;
	protected String javaxProtocolHandlerPKGS;
	protected String javaxNETDebug;
	protected String comSunManagementJMXRemoteAuthenticate;
	protected String comSunManagementJMXRemoteSSL;
	protected String comSunManagementJMXRemotePort;
	protected String javaRMIServerHostname;
	protected boolean network = true;
	protected Input input = null;
	protected Output output = null;
	protected Delay delay = null;

	public static void main(String[] args) {
		Network network = new Network();
		CountDownLatch countDownLatch;
		network.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		network.start();
	}

	public Network() {
		super();

	}

	public Network(URL[] urlArray, MBeanServer mBeanServer) {
		super(urlArray, mBeanServer);
	}

	public Network(Integer id, Module module) {
		super(id, module);
	}

	public void initialize() {
		super.initialize();
		this.timeout = Utility.stringToInteger(getProperty("@timeout"));
		this.connection = getProperty("@connection", null);
		this.acknowledgeDelay = Utility.stringToDouble(getProperty("acknowledgeDelay"));
		this.stateMap.put(CONNECTION, "CONNECTION");
		this.connectionDelay = Utility.stringToDouble(getProperty("@connectionDelay"));
		this.hostAddress = getProperty("#hostAddress");
		this.port = Utility.stringToInteger(getProperty("#port"));
		this.path = getProperty("#path");
		this.httpURL = newHTTPURL(this.hostAddress, this.port, this.path);
		this.javaxNETSSLKeyStorePath = getProperty("@javaxNETSSLKeyStorePath");
		this.javaxNETSSLKeyStorePassword = getProperty("@javaxNETSSLKeyStorePassword");
		this.javaxNETSSLTrustStorePath = getProperty("@javaxNETSSLTrustStorePath");
		this.javaxNETSSLTrustStorePassword = getProperty("@javaxNETSSLTrustStorePassword");
		this.javaxProtocolHandlerPKGS = getProperty("@javaxProtocolHandlerPKGS");
		this.javaxNETDebug = getProperty("@javaxNETDebug");
		this.comSunManagementJMXRemoteAuthenticate = getProperty("@comSunManagementJMXRemoteAuthenticate");
		this.comSunManagementJMXRemoteSSL = getProperty("@comSunManagementJMXRemoteSSL");
		this.comSunManagementJMXRemotePort = getProperty("@comSunManagementJMXRemotePort");
		this.javaRMIServerHostname = getProperty("@javaRMIServerHostname");
		if (StringUtils.isNotBlank(this.comSunManagementJMXRemoteAuthenticate)) {
			System.setProperty("com.sun.management.jmxremote.authenticate", this.comSunManagementJMXRemoteAuthenticate);
		}
		if (StringUtils.isNotBlank(this.comSunManagementJMXRemotePort)) {
			System.setProperty("com.sun.management.jmxremote.port", this.comSunManagementJMXRemotePort);
		}
		if (StringUtils.isNotBlank(this.comSunManagementJMXRemoteSSL)) {
			System.setProperty("com.sun.management.jmxremote.ssl", this.comSunManagementJMXRemoteSSL);
		}
		if (StringUtils.isNotBlank(this.javaRMIServerHostname)) {
			System.setProperty("java.rmi.server.hostname", this.javaRMIServerHostname);
		}
		if ((StringUtils.isNotBlank(this.connection)) && (this.connection.equalsIgnoreCase("sslsocket"))) {
			if (!StringUtils.isBlank(this.javaxNETSSLKeyStorePath)) {
				if (logger.isDebugEnabled()) {
					logger.debug("initialize() (this.javaNETSSLKeyStorePath= " + this.javaxNETSSLKeyStorePath + ")");
				}
				File javaxNETSSLKeyStoreFile = new File(this.javaxNETSSLKeyStorePath);
				if (javaxNETSSLKeyStoreFile.exists()) {
					if (logger.isDebugEnabled()) {
						logger.debug("initialize() (javaNETSSLKeyStoreFile= " + javaxNETSSLKeyStoreFile + ")");
					}
					System.setProperty("javax.net.ssl.keyStore", this.javaxNETSSLKeyStorePath);
				} else {
					logger.warn("initialize() (!javaNETSSLKeyStoreFile.exists())");
				}
			}
			if (StringUtils.isNotBlank(this.javaxNETSSLKeyStorePassword)) {
				if (logger.isDebugEnabled()) {
					logger.debug("initialize() (this.javaxNETSSLKeyStorePassword = ********)");
				}
				System.setProperty("javax.net.ssl.keyStorePassword", this.javaxNETSSLKeyStorePassword);
			}
			if (StringUtils.isNotBlank(this.javaxNETSSLTrustStorePath)) {
				if (logger.isDebugEnabled()) {
					logger.debug("initialize() (this.javaxNETSSLTrustStore = " + this.javaxNETSSLTrustStorePath + ")");
				}
				File javaxNETSSLTrustStoreFile = new File(this.javaxNETSSLTrustStorePath);
				if (javaxNETSSLTrustStoreFile.exists()) {
					if (logger.isDebugEnabled()) {
						logger.debug("initialize() (javaNETSSLTrustStoreFile= " + javaxNETSSLTrustStoreFile + ")");
					}
					System.setProperty("javax.net.ssl.trustStore", this.javaxNETSSLTrustStorePath);
				} else {
					logger.warn("initialize() (!javaNETSSLTrustStoreFile.exists())");
				}
			}
			if (StringUtils.isNotBlank(this.javaxNETSSLTrustStorePassword)) {
				if (logger.isDebugEnabled()) {
					logger.debug("initialize() (this.javaxNETSSLTrustStorePassword = ********)");
				}
				System.setProperty("javax.net.ssl.trustStorePassword", this.javaxNETSSLTrustStorePassword);
			}
			if (StringUtils.isNotBlank(this.javaxProtocolHandlerPKGS)) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"initialize() (this.javaxProtocolHandlerPKGS = " + this.javaxProtocolHandlerPKGS + ")");
				}
				System.setProperty("javax.protocol.handler.pkgs", this.javaxProtocolHandlerPKGS);
			}
			if (StringUtils.isNotBlank(this.javaxNETDebug)) {
				if (logger.isDebugEnabled()) {
					logger.debug("initialize() (this.javaxNETDebug = " + this.javaxNETDebug + ")");
				}
				System.setProperty("javax.net.debug", this.javaxNETDebug);
			}
		}
	}

	public void destroy() {
		if (!this.destroy) {
			super.destroy();
			socketClose(this.socket);
			serverSocketClose(this.serverSocket);
			sslSocketClose(this.sslSocket);
			sslServerSocketClose(this.sslServerSocket);
		}
	}
	
	@Override
	protected void machine(int state, Object object) {
		switch (state) {
		case CONNECTION:
			connectionState(object);
			break;
		default:
			super.machine(state, object);
		}
	}
	
	@Override
	protected void inputState(Object object) {
		if (input()) {
			if ((object instanceof Data)) {
				if (logger.isDebugEnabled()) {
					logger.trace("inputState(" + object + ") (object instanceof Container)");
				}
				Data data = (Data) object;
				object = data.getObject();
				switch (data.getType()) {
				case Data.OUTPUT:
					output(object);
					break;
				case Data.INPUT:
					input(object);
					break;
				case Data.ACKNOWLEDGE:
					acknowledge(object);
				case 10:
					poll(data, this.poll);
				}
			}
		} else {
			setState(0);
		}
	}
	
	protected boolean input() {
		boolean input = true;
		if (this.moduleMap.size() < this.moduleMapSize) {
			if (logger.isDebugEnabled()) {
				logger.debug("input() (this.moduleMap.size()<this.moduleMapSize)");
			}
			input = false;
		}
		return input;
	}

	protected void connectionState(Object object) {
		if ((object instanceof Data)) {
			Data data = (Data) object;
			switch (data.getType()) {
			case 10:
				poll(data, false);
			}
		}
		if (delayExpired()) {
			setDelay(newDelay(this.connectionDelay));
			if (getTry()) {
				if (connection()) {
					poll(null, true);
					setDelay(newDelay(this.inputDelay));
					setState(2);
				}
			} else {
				setState(0);
			}
		}
	}
	
	protected boolean getTry() {
		boolean flag = true;
		if (this.tryMax > 0) {
			if (this.tryMin < this.tryMax) {
				if (logger.isDebugEnabled()) {
					logger.debug("getTry() ((tryMin = " + this.tryMin + ") < (tryMax = " + this.tryMax + "))");
				}
				this.tryMin += 1;
			} else {
				if (logger.isDebugEnabled()) {
					logger.warn("getTry(" + this.tryMin + ", " + this.tryMax + ") (tryMin>=tryMax)");
				}
				flag = false;
			}
		}
		return flag;
	}
//	
//	protected boolean connection(Object object) {
//		return connectionStart();
//	}

	protected boolean connectionStart() {
		boolean flag = false;
		int count = 0;
		if (this.input != null) {
			count++;
			if (logger.isDebugEnabled()) {
				logger.debug("connectionStart() (this.input == " + this.input + ")");
			}
		}
		if (this.output != null) {
			count++;
			if (logger.isDebugEnabled()) {
				logger.debug("connectionStart() (this.output == " + this.output + ")");
			}
		}
		if (this.delay != null) {
			count++;
			if (logger.isDebugEnabled()) {
				logger.debug("connectionStart() (this.delay == " + this.delay + ")");
			}
		}
		CountDownLatch countDownLatch = new CountDownLatch(count);
		if (this.input != null) {
			this.input.setCountDownLatch(countDownLatch);
		}
		if (this.output != null) {
			this.output.setCountDownLatch(countDownLatch);
		}
		if (this.delay != null) {
			this.delay.setCountDownLatch(countDownLatch);
		}
		this.moduleMapSize = this.moduleMap.size();
		moduleMapStart(this.moduleMap);
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("connectionStart() (countDownLatch.await())");
			}
			countDownLatch.await();
			flag = true;
		} catch (InterruptedException ie) {
			logger.error("connectionStart() InterruptedException");
		}
		return flag;
	}

	protected Protocol newProtocol() {
		return new Protocol();
	}

	protected void output(Object object) {
		if (logger.isDebugEnabled()) {
			logger.debug("output(" + object + ")");
		}
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			if (this.delay != null) {
				if (protocol.getMessageOffset() < this.protocol.getMessageOffset()) {
					logger.warn("output(" + object + ") ((protocol.getMessageOffset() = " + protocol.getMessageOffset()
							+ ") < (this.messegeOffset = " + this.protocol.getMessageOffset() + "))");
				} else if (protocol.getTryCount() > this.tryMax) {
					logger.warn("output(" + object + ") ((protocol.getTryCount() = " + protocol.getTryCount()
							+ ") > (this.MAX_TRIES = " + this.tryMax + "))");
					setState(0);
				} else {
					this.output.add(
							new Data(this.id.intValue(), this.id.intValue(), 1, 0.0D, protocol, null));
					int protocolDataLength;
					if ((protocolDataLength = protocol.getDataLength()) > 0) {
						this.protocol.setMessageOffset(this.protocol.getMessageOffset() + protocolDataLength);
						protocol.setTryCount(protocol.getTryCount() + 1);
						this.delay.add(new Data(this.id.intValue(), this.id.intValue(), 1,
								protocol.getTimeout() * protocol.getTryCount(), protocol, this.objectList));
					}
				}
			} else {
				this.output.add(
						new Data(this.id.intValue(), this.id.intValue(), 1, 0.0D, protocol, null));
			}
		}
	}

	protected void input(Object object) {
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			switch (protocol.getType()) {
			case 2:
				protocolSetMessageAcknowledged(object);
				break;
			case 3:
				if (protocolSetMessageAcknowledged(object)) {
					delayAcknowledge(object);
				}
				outputProtocolAdvertisement();
				break;
			case 5:
				if (logger.isDebugEnabled()) {
					logger.debug("input(" + object + ") Protocol.DISCONNECT");
				}
				setState(0);
			}
		}
	}

	protected void acknowledge(Object object) {
		if (((object instanceof Integer)) && (shouldAcknowledge(((Integer) object).intValue()))) {
			outputProtocolAdvertisement();
		}
	}

	protected void protocol(Protocol protocol) {
		inputData(protocol.getObject());
	}

	protected boolean shouldAcknowledge(int messageAcknowledged) {
		boolean flag = false;
		if (messageAcknowledged > this.protocol.getMessageAcknowledged()) {
			flag = true;
		}
		return flag;
	}

	protected void delayAcknowledge(Object object) {
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			if (protocol.getDataLength() > 0) {
				protocol(protocol);
				this.delay.add(new Data(this.id.intValue(), this.id.intValue(), 1233,
						this.acknowledgeDelay, Integer.valueOf(protocol.getMessageOffset() + protocol.getDataLength()),
						this.objectList));
			}
		}
	}

	protected boolean protocolSetMessageAcknowledged(Object object) {
		boolean flag = false;
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			if (protocol.getMessageAcknowledged() > this.protocol.getMessageOffset()) {
				this.protocol.setMessageOffset(protocol.getMessageAcknowledged());
			}
			if (protocol.getMessageOffset() == this.protocol.getMessageAcknowledged()) {
				this.protocol.setMessageAcknowledged(this.protocol.getMessageAcknowledged() + protocol.getDataLength());
				flag = true;
			}
		}
		return flag;
	}

	protected void outputProtocolDisconnect() {
		Protocol protocol = new Protocol();
		protocol.serialize(5, this.protocol.getMessageOffset(), this.protocol.getMessageAcknowledged(), "");
		Data data = new Data(this.id.intValue(), this.id.intValue(), 1, 0.0D, protocol, null);
		this.output.add(data);
	}

	protected void outputProtocolAdvertisement() {
		Protocol protocol = new Protocol();
		protocol.serialize(2, this.protocol.getMessageOffset(), this.protocol.getMessageAcknowledged(), "");
		this.output.add(new Data(this.id.intValue(), this.id.intValue(), 1, 0.0D, protocol, null));
	}

	protected boolean connection() {
		boolean connection = false;
		if (StringUtils.isNotBlank(this.connection)) {
			if (this.connection.equalsIgnoreCase("sslsocket")) {
				if ((execute("ping -c 3 " + this.hostAddress) == 0)
						&& ((this.sslSocket = getSSLSocket(this.hostAddress, this.port, this.timeout)) != null)) {
					connection = connection(this.sslSocket);
				}
			} else if (this.connection.equalsIgnoreCase("socket")) {
				if ((execute("ping -c 3 " + this.hostAddress) == 0)
						&& ((this.socket = getSocket(this.hostAddress, this.port, this.timeout)) != null)) {
					connection = connection(this.socket);
				}
			} else if (this.connection.equalsIgnoreCase("sslserversocket")) {
				if ((this.sslServerSocket = newSSLServerSocket(this.port, this.timeout)) != null) {
					if ((this.sslSocket = getSSLSocket(this.sslServerSocket)) != null) {
						connection = connection(this.sslSocket);
					}
				}
			} else if (this.connection.equalsIgnoreCase("serversocket")) {
				if ((this.serverSocket = newServerSocket(this.port, this.timeout)) != null) {
					if ((this.socket = getSocket(this.serverSocket)) != null) {
						connection = connection(this.socket);
					}
				}
			} else {
				connection = false;
			}
		}
		return connection;
	}

	protected boolean connection(Object object) {
		if ((object instanceof SSLSocket)) {
			if (logger.isDebugEnabled()) {
				logger.info("connection(" + object + ") (object instanceof SSLSocket)");
			}
			SSLSocket sslSocket = (SSLSocket) object;
			this.input = new Input(this.id.intValue(), this, getSSLSocketInputStream(sslSocket));
			this.output = new Output(this.id.intValue(), this, getSSLSocketOutputStream(sslSocket));
			this.delay = new Delay(this.id.intValue(), this);
		} else if ((object instanceof Socket)) {
			if (logger.isDebugEnabled()) {
				logger.debug("connection(" + object + ") (object instanceof Socket)");
			}
			Socket socket = (Socket) object;
			this.input = new Input(this.id.intValue(), this, getSocketInputStream(socket));
			this.output = new Output(this.id.intValue(), this, getSocketOutputStream(socket));
			this.delay = new Delay(this.id.intValue(), this);
		}
		return false;//super.connection(object);
	}

	protected String newHTTPURL(String hostAddress, int port, String path) {
		String url = null;
		if ((!StringUtils.isBlank(hostAddress)) && (port > 0) && (port < 65535) && (!StringUtils.isBlank(path))) {
			url = newHTTPURL(hostAddress, port, path);
		}
		return url;
	}

	protected String newHTTPURL(String hostAddress, String port, String path) {
		String httpURL = "";
		if ((!StringUtils.isBlank(hostAddress)) && (!StringUtils.isBlank(port)) && (!StringUtils.isBlank(path))) {
			httpURL = "http://" + hostAddress + ":" + port + path;
		}
		return httpURL;
	}

	protected static InputStream getSSLSocketInputStream(SSLSocket sslSocket) {
		InputStream inputStream = null;
		try {
			inputStream = sslSocket.getInputStream();
		} catch (IOException e) {
			logger.warn("getSSLSocketInputStream(" + sslSocket + ") IOException");
		}
		return inputStream;
	}

	protected static InputStream getSocketInputStream(Socket socket) {
		InputStream inputStream = null;
		try {
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			logger.warn("getSocketInputStream(" + socket + ") IOException");
		}
		return inputStream;
	}

	protected static OutputStream getSSLSocketOutputStream(SSLSocket sslSocket) {
		OutputStream outputStream = null;
		if (sslSocket != null) {
			try {
				outputStream = sslSocket.getOutputStream();
			} catch (IOException e) {
				logger.warn("getSSLSocketOutputStream(" + sslSocket + ") IOException");
			}
		}
		return outputStream;
	}

	protected static OutputStream getSocketOutputStream(Socket socket) {
		OutputStream outputStream = null;
		if (socket != null) {
			try {
				outputStream = socket.getOutputStream();
			} catch (IOException e) {
				logger.warn("getSocketOutputStream(" + socket + ") IOException");
			}
		}
		return outputStream;
	}

	protected void sslSocketClose(SSLSocket sslSocket) {
		if (sslSocket != null) {
			try {
				sslSocket.close();
			} catch (IOException e) {
				logger.warn("sslSocketClose(" + sslSocket + ") IOException");
			}
		}
	}

	protected void socketClose(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warn(this + ".socketClose(" + socket + ") IOException");
			}
		}
	}

	protected SSLSocket getSSLSocket(String hostAddress, int port, int timeout) {
		return newSSLSocket(hostAddress, port, timeout);
	}

	protected Socket getSocket(String hostAddress, int port, int timeout) {
		return newSocket(hostAddress, port, timeout);
	}

	protected SSLSocket newSSLSocket(String hostAddress, int port, int timeout) {
		SSLSocket sslSocket = null;
		if ((hostAddress != null) && (port > -1) && (timeout > -1)) {
			SSLSocketFactory ssLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			try {
				sslSocket = (SSLSocket) ssLSocketFactory.createSocket();
				sslSocket.connect(new InetSocketAddress(hostAddress, port), timeout);
			} catch (UnknownHostException e) {
				logger.warn("newSSlSocket(" + hostAddress + ", " + port + ", " + timeout + ") UnknownHostException");
				sslSocket = null;
			} catch (IOException e) {
				logger.warn("newSSlSocket(" + hostAddress + ", " + port + ", " + timeout + ") IOException");
				sslSocket = null;
			}
		}
		if ((sslSocket != null) && (!sslSocket.isBound())) {
			sslSocket = null;
		}
		if ((sslSocket != null) && (!sslSocket.isConnected())) {
			sslSocket = null;
		}
		if ((sslSocket != null) && (sslSocket.isClosed())) {
			sslSocket = null;
		}
		return sslSocket;
	}

	protected Socket newSocket(String hostAddress, int port, int timeout) {
		Socket socket = null;
		if ((hostAddress != null) && (port > -1) && (timeout > -1)) {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(hostAddress, port), timeout);
			} catch (UnknownHostException e) {
				logger.warn("newSocket(" + hostAddress + ", " + port + ", " + timeout + ") UnknownHostException");
				socket = null;
			} catch (IOException e) {
				logger.warn("newSocket(" + hostAddress + ", " + port + ", " + timeout + ") IOException");
				socket = null;
			}
		}
		if ((socket != null) && (!socket.isBound())) {
			socket = null;
		}
		if ((socket != null) && (!socket.isConnected())) {
			socket = null;
		}
		if ((socket != null) && (socket.isClosed())) {
			socket = null;
		}
		return socket;
	}

	protected SSLSocket getSSLSocket(int port, int timeout) {
		SSLSocket sslSocket = null;
		SSLServerSocket sslServerSocket = newSSLServerSocket(port, timeout);
		if (sslServerSocket != null) {
			sslSocket = sslServerSocketAccept(sslServerSocket);
			sslServerSocketClose(sslServerSocket);
		}
		return sslSocket;
	}

	protected Socket getSocket(int port, int timeout) {
		Socket socket = null;
		ServerSocket serverSocket = newServerSocket(port, timeout);
		socket = getSocket(serverSocket);
		return socket;
	}

	protected Socket getSocket(ServerSocket serverSocket) {
		Socket socket = null;
		if (serverSocket != null) {
			socket = serverSocketAccept(serverSocket);
			serverSocketClose(serverSocket);
		}
		return socket;
	}

	protected SSLSocket getSSLSocket(SSLServerSocket sslServerSocket) {
		SSLSocket sslSocket = null;
		if (sslServerSocket != null) {
			sslSocket = sslServerSocketAccept(sslServerSocket);
			sslServerSocketClose(sslServerSocket);
		}
		return sslSocket;
	}

	protected SSLSocket sslServerSocketAccept(SSLServerSocket sslServerSocket) {
		SSLSocket sslSocket = null;
		try {
			sslSocket = (SSLSocket) sslServerSocket.accept();
		} catch (IOException e) {
			logger.warn("sslServerSocketAccept(" + sslServerSocket + ") IOException");
		}
		return sslSocket;
	}

	protected Socket serverSocketAccept(ServerSocket serverSocket) {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			logger.warn("serverSocketAccept(" + serverSocket + ") IOException");
		}
		return socket;
	}

	protected SSLServerSocket newSSLServerSocket(int port, int timeout) {
		if (logger.isDebugEnabled()) {
			logger.debug("newSSLServerSocket(" + port + ", " + timeout + ")");
		}
		SSLServerSocket sslServerSocket = null;
		if ((port != -1) && (timeout != -1) && (port > 0) && (port < 65535)) {
			SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			try {
				sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
				sslServerSocketSetSoTimeout(sslServerSocket, timeout);
			} catch (IOException e) {
				logger.warn("newSSLServerSocket(" + port + ", " + timeout + ") IOException");
			}
		}
		if (sslServerSocket != null) {
			if (!sslServerSocket.isBound()) {
				sslServerSocket = null;
			}
			if (sslServerSocket.isClosed()) {
				sslServerSocket = null;
			}
		}
		return sslServerSocket;
	}

	protected ServerSocket newServerSocket(int port, int timeout) {
		if (logger.isDebugEnabled()) {
			logger.debug("newServerSocket(" + port + ", " + timeout + ")");
		}
		ServerSocket serverSocket = null;
		if ((port != -1) && (timeout != -1) && (port > 0) && (port < 65535)) {
			try {
				serverSocket = new ServerSocket(port);
				serverSocketSetSoTimeout(serverSocket, timeout);
			} catch (IOException e) {
				logger.warn("newServerSocket(" + port + ", " + timeout + ") IOException");
			}
		}
		if (serverSocket != null) {
			if (!serverSocket.isBound()) {
				serverSocket = null;
			}
			if (serverSocket.isClosed()) {
				serverSocket = null;
			}
		}
		return serverSocket;
	}

	protected void sslServerSocketClose(SSLServerSocket sslServerSocket) {
		if (logger.isDebugEnabled()) {
			logger.debug("sslServerSocketClose(" + sslServerSocket + ")");
		}
		if (sslServerSocket != null) {
			try {
				sslServerSocket.close();
			} catch (IOException e) {
				logger.warn("sslServerSocketClose(" + sslServerSocket + ") IOException");
			}
		}
	}

	protected void serverSocketClose(ServerSocket serverSocket) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + ".serverSocketClose(" + serverSocket + ")");
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				logger.warn(this + ".serverSocketClose(" + serverSocket + ") IOException");
			}
		}
	}

	protected void sslServerSocketSetSoTimeout(SSLServerSocket sslServerSocket, int timeout) {
		try {
			sslServerSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			logger.warn("sslServerSocketSetSoTimeout(" + sslServerSocket + ", " + timeout + ") SocketException");
		}
	}

	protected void serverSocketSetSoTimeout(ServerSocket serverSocket, int timeout) {
		try {
			serverSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			logger.warn("serverSocketSetSoTimeout(" + serverSocket + ", " + timeout + ") SocketException");
		}
	}

	protected InputStream downloadFile(String fileURL) {
		logger.warn("downloadFile(" + fileURL + ")");

		InputStream inputStream = null;
		if (fileURL != null) {
			try {
				URL url = new URL(fileURL);
				URLConnection urlConnection = url.openConnection();
				inputStream = urlConnection.getInputStream();
			} catch (MalformedURLException e) {
				logger.error("downloadFile(" + fileURL + ") MalformedURLException");
			} catch (IOException e) {
				logger.error("downloadFile(" + fileURL + ") IOException");
			}
		}
		return inputStream;
	}
}

/*
 * Copyright 2020 Joaquin Osvaldo Rodriguez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meritoki.module.library.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang3.StringUtils;

import com.meritoki.module.library.model.protocol.Protocol;
import com.meritoki.module.library.model.protocol.ProtocolType;

public class Web extends Network {

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

	public static void main(String[] args) {
		Web network = new Web();
		CountDownLatch countDownLatch;
		network.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		network.start();
	}

	public Web() {
		super();

	}

	public Web(int id) {
		super(id);

	}

	public Web(Integer id, Module module) {
		super(id, module);
	}

	public void initialize() {
		super.initialize();
		this.hostAddress = (this.hostAddress == null)?getProperty("#hostAddress"):this.hostAddress;
		this.port = (this.port == -1)?Utility.stringToInteger(getProperty("#port")):this.port;
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
				 {
					logger.fine("initialize() (this.javaNETSSLKeyStorePath= " + this.javaxNETSSLKeyStorePath + ")");
				}
				File javaxNETSSLKeyStoreFile = new File(this.javaxNETSSLKeyStorePath);
				if (javaxNETSSLKeyStoreFile.exists()) {
					 {
						logger.fine("initialize() (javaNETSSLKeyStoreFile= " + javaxNETSSLKeyStoreFile + ")");
					}
					System.setProperty("javax.net.ssl.keyStore", this.javaxNETSSLKeyStorePath);
				} else {
					logger.warning("initialize() (!javaNETSSLKeyStoreFile.exists())");
				}
			}
			if (StringUtils.isNotBlank(this.javaxNETSSLKeyStorePassword)) {
				 {
					logger.fine("initialize() (this.javaxNETSSLKeyStorePassword = ********)");
				}
				System.setProperty("javax.net.ssl.keyStorePassword", this.javaxNETSSLKeyStorePassword);
			}
			if (StringUtils.isNotBlank(this.javaxNETSSLTrustStorePath)) {
				 {
					logger.fine("initialize() (this.javaxNETSSLTrustStore = " + this.javaxNETSSLTrustStorePath + ")");
				}
				File javaxNETSSLTrustStoreFile = new File(this.javaxNETSSLTrustStorePath);
				if (javaxNETSSLTrustStoreFile.exists()) {
					 {
						logger.fine("initialize() (javaNETSSLTrustStoreFile= " + javaxNETSSLTrustStoreFile + ")");
					}
					System.setProperty("javax.net.ssl.trustStore", this.javaxNETSSLTrustStorePath);
				} else {
					logger.warning("initialize() (!javaNETSSLTrustStoreFile.exists())");
				}
			}
			if (StringUtils.isNotBlank(this.javaxNETSSLTrustStorePassword)) {
				 {
					logger.fine("initialize() (this.javaxNETSSLTrustStorePassword = ********)");
				}
				System.setProperty("javax.net.ssl.trustStorePassword", this.javaxNETSSLTrustStorePassword);
			}
			if (StringUtils.isNotBlank(this.javaxProtocolHandlerPKGS)) {
				 {
					logger.fine(
							"initialize() (this.javaxProtocolHandlerPKGS = " + this.javaxProtocolHandlerPKGS + ")");
				}
				System.setProperty("javax.protocol.handler.pkgs", this.javaxProtocolHandlerPKGS);
			}
			if (StringUtils.isNotBlank(this.javaxNETDebug)) {
				 {
					logger.fine("initialize() (this.javaxNETDebug = " + this.javaxNETDebug + ")");
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
    protected void output(Object object) {
        if(object instanceof String) {
            String string = (String) object;
            Protocol protocol = new Protocol();
            protocol.serialize(ProtocolType.MESSAGE,this.protocol.getMessageOffset(),this.protocol.getMessageAcknowledged(),string);
            super.output(protocol);
        }
    }

	protected boolean connection() {
		boolean connection = false;
		if (StringUtils.isNotBlank(this.connection)) {
			if (this.connection.equalsIgnoreCase("sslsocket")) {
				if (((this.sslSocket = getSSLSocket(this.hostAddress, this.port, this.timeout)) != null)) {
					connection = connection(this.sslSocket);
				}
			} else if (this.connection.equalsIgnoreCase("socket")) {
				if (((this.socket = getSocket(this.hostAddress, this.port, this.timeout)) != null)) {
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

	@Override
	protected boolean connection(Object object) {
		if ((object instanceof SSLSocket)) {
			 {
				logger.info("connection(" + object + ") (object instanceof SSLSocket)");
			}
			SSLSocket sslSocket = (SSLSocket) object;
			this.input = newInput(this.id.intValue(), this, getSSLSocketInputStream(sslSocket));
			this.output = newOutput(this.id.intValue(), this, getSSLSocketOutputStream(sslSocket));
			this.delay = newDelay(this.id.intValue(), this);
		} else if ((object instanceof Socket)) {
			 {
				logger.fine("connection(" + object + ") (object instanceof Socket)");
			}
			Socket socket = (Socket) object;
			this.input = newInput(this.id.intValue(), this, getSocketInputStream(socket));
			this.output = newOutput(this.id.intValue(), this, getSocketOutputStream(socket));
			this.delay = newDelay(this.id.intValue(), this);
		}
		return this.connectionStart();
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

	protected InputStream getSSLSocketInputStream(SSLSocket sslSocket) {
		InputStream inputStream = null;
		try {
			inputStream = sslSocket.getInputStream();
		} catch (IOException e) {
			logger.warning("getSSLSocketInputStream(" + sslSocket + ") IOException");
		}
		return inputStream;
	}

	protected InputStream getSocketInputStream(Socket socket) {
		InputStream inputStream = null;
		try {
			inputStream = socket.getInputStream();
		} catch (IOException e) {
			logger.warning("getSocketInputStream(" + socket + ") IOException");
		}
		return inputStream;
	}

	protected OutputStream getSSLSocketOutputStream(SSLSocket sslSocket) {
		OutputStream outputStream = null;
		if (sslSocket != null) {
			try {
				outputStream = sslSocket.getOutputStream();
			} catch (IOException e) {
				logger.warning("getSSLSocketOutputStream(" + sslSocket + ") IOException");
			}
		}
		return outputStream;
	}

	protected OutputStream getSocketOutputStream(Socket socket) {
		OutputStream outputStream = null;
		if (socket != null) {
			try {
				outputStream = socket.getOutputStream();
			} catch (IOException e) {
				logger.warning("getSocketOutputStream(" + socket + ") IOException");
			}
		}
		return outputStream;
	}

	protected void sslSocketClose(SSLSocket sslSocket) {
		if (sslSocket != null) {
			try {
				sslSocket.close();
			} catch (IOException e) {
				logger.warning("sslSocketClose(" + sslSocket + ") IOException");
			}
		}
	}

	protected void socketClose(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warning(this + ".socketClose(" + socket + ") IOException");
			}
		}
	}

	protected SSLSocket getSSLSocket(String hostAddress, int port, int timeout) {
		SSLSocket sslSocket = null;
		if ((hostAddress != null) && (port > -1) && (timeout > -1)) {
			SSLSocketFactory ssLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			try {
				sslSocket = (SSLSocket) ssLSocketFactory.createSocket();
				sslSocket.connect(new InetSocketAddress(hostAddress, port), timeout);
			} catch (UnknownHostException e) {
				logger.warning("newSSlSocket(" + hostAddress + ", " + port + ", " + timeout + ") UnknownHostException");
				sslSocket = null;
			} catch (IOException e) {
				logger.warning("newSSlSocket(" + hostAddress + ", " + port + ", " + timeout + ") IOException");
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

	protected Socket getSocket(String hostAddress, int port, int timeout) {
		logger.info("getSocket(" + hostAddress + ", " + port + ", " + timeout + ")");
		Socket socket = null;
		if ((hostAddress != null) && (port > -1) && (timeout > -1)) {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(hostAddress, port), timeout);
			} catch (UnknownHostException e) {
				logger.warning("getSocket(" + hostAddress + ", " + port + ", " + timeout + ") UnknownHostException");
				socket = null;
			} catch (IOException e) {
				logger.warning("getSocket(" + hostAddress + ", " + port + ", " + timeout + ") IOException");
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
			logger.warning("sslServerSocketAccept(" + sslServerSocket + ") IOException");
		}
		return sslSocket;
	}

	protected Socket serverSocketAccept(ServerSocket serverSocket) {
		logger.info("serverSocketAccept(" + serverSocket + ")");
		Socket socket = null;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			logger.warning("serverSocketAccept(" + serverSocket + ") IOException");
		}
		return socket;
	}

	protected SSLServerSocket newSSLServerSocket(int port, int timeout) {
		 {
			logger.fine("newSSLServerSocket(" + port + ", " + timeout + ")");
		}
		SSLServerSocket sslServerSocket = null;
		if ((port != -1) && (timeout != -1) && (port > 0) && (port < 65535)) {
			SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			try {
				sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
				sslServerSocketSetSoTimeout(sslServerSocket, timeout);
			} catch (IOException e) {
				logger.warning("newSSLServerSocket(" + port + ", " + timeout + ") IOException");
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
		 {
			logger.fine("newServerSocket(" + port + ", " + timeout + ")");
		}
		ServerSocket serverSocket = null;
		if ((port != -1) && (timeout != -1) && (port > 0) && (port < 65535)) {
			try {
				serverSocket = new ServerSocket(port);
				serverSocketSetSoTimeout(serverSocket, timeout);
			} catch (IOException e) {
				logger.warning("newServerSocket(" + port + ", " + timeout + ") IOException");
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
		 {
			logger.fine("sslServerSocketClose(" + sslServerSocket + ")");
		}
		if (sslServerSocket != null) {
			try {
				sslServerSocket.close();
			} catch (IOException e) {
				logger.warning("sslServerSocketClose(" + sslServerSocket + ") IOException");
			}
		}
	}

	protected void serverSocketClose(ServerSocket serverSocket) {
		 {
			logger.fine(this + ".serverSocketClose(" + serverSocket + ")");
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				logger.warning(this + ".serverSocketClose(" + serverSocket + ") IOException");
			}
		}
	}

	protected void sslServerSocketSetSoTimeout(SSLServerSocket sslServerSocket, int timeout) {
		try {
			sslServerSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			logger.warning("sslServerSocketSetSoTimeout(" + sslServerSocket + ", " + timeout + ") SocketException");
		}
	}

	protected void serverSocketSetSoTimeout(ServerSocket serverSocket, int timeout) {
		logger.info("serverSocketSetSoTimeout(" + serverSocket + ", " + timeout + ")");
		try {
			serverSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			logger.warning("serverSocketSetSoTimeout(" + serverSocket + ", " + timeout + ") SocketException");
		}
	}
}

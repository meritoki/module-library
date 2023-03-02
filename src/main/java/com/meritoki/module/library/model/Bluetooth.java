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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.meritoki.module.library.model.protocol.Protocol;
import com.meritoki.module.library.model.protocol.ProtocolType;

public class Bluetooth extends Network {
	protected Logger logger = LoggerFactory.getLogger(Bluetooth.class.getName());
	private String deviceUUID;
	private String serviceName;
	protected StreamConnection streamConnection;
	private StreamConnectionNotifier streamConnectionNotifier;

	public static void main(String[] args) {
		Bluetooth bluetooth = new Bluetooth(0);
		CountDownLatch countDownLatch;
		bluetooth.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		bluetooth.start();
	}

	public Bluetooth() {
		super();

	}

	public Bluetooth(int id) {
		super(id);
	}

	public Bluetooth(Integer id, Module module) {
		super(id, module);
	}

	@Override
	public void initialize() {
		super.initialize();
		this.deviceUUID = getProperty("@deviceUUID");// "11111111111111111111111111111123";
		this.serviceName = getProperty("@serviceName");
		logger.info("initialize() this.deviceUUID=" + this.deviceUUID);
		logger.info("initialize() this.serviceName=" + this.serviceName);
	}

	@Override
	protected boolean connection() {
		boolean flag = false;
		if (StringUtils.isNotBlank(this.connection)) {
			if (this.connection.equalsIgnoreCase("stream")) {
				LocalDevice localDevice;
				try {
					localDevice = LocalDevice.getLocalDevice();
					localDevice.setDiscoverable(DiscoveryAgent.GIAC); // Advertising the service
					String url = "btspp://localhost:" + new UUID(deviceUUID, false)
							+ ";authenticate=false;encrypt=false;name="+this.serviceName;
					System.out.println(url);
					this.streamConnectionNotifier = (StreamConnectionNotifier) Connector.open(url);
					this.streamConnection = this.streamConnectionNotifier.acceptAndOpen(); // Wait until client
																									// connects
					flag = connection(this.streamConnection);
				} catch (BluetoothStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

	@Override
	protected boolean connection(Object object) {
		if (object instanceof StreamConnection) {
			StreamConnection streamConnection = (StreamConnection) object;
			this.input = newInput(this.id.intValue(), this, this.getStreamConnectionInputStream(streamConnection));
			this.output = newOutput(this.id.intValue(), this, this.getStreamConnectionOutputStream(streamConnection));
			this.delay = newDelay(this.id.intValue(), this);
		}
		return this.connectionStart();
	}

	public InputStream getStreamConnectionInputStream(StreamConnection streamConnection) {
		InputStream inputStream = null;
		try {
			inputStream = streamConnection.openInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputStream;
	}

	public OutputStream getStreamConnectionOutputStream(StreamConnection streamConnection) {
		OutputStream outputStream = null;
		try {
			outputStream = streamConnection.openOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outputStream;
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
	
    @Override
	protected void input(Object object) {
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			switch (protocol.getType()) {
			case ADVERTISEMENT: {
				protocolSetMessageAcknowledged(object);
				break;
			}
			case MESSAGE: {
				if (protocolSetMessageAcknowledged(object)) {
					delayAcknowledge(object);
				}
				outputProtocolAdvertisement();
				break;
			}
			case DISCONNECT:{
				logger.info("input(" + object + ") Protocol.DISCONNECT");
				try {
					this.streamConnection.close();
					this.streamConnectionNotifier.close();
				} catch (IOException e) {
					logger.warn("IOException "+e.getMessage());
				}
				this.input.destroy();
				this.output.destroy();
				this.delay.destroy();
				setState(State.CONNECTION);
				break;
			}
			default: {
				break;
			}
			}
		}
	}
}

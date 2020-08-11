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

public class Bluetooth extends Network {

	private String deviceUUID;
	private String serviceName;

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
					StreamConnectionNotifier streamConnectionNotifier = (StreamConnectionNotifier) Connector.open(url);
					StreamConnection streamConnection = streamConnectionNotifier.acceptAndOpen(); // Wait until client
																									// connects
					flag = connection(streamConnection);
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
}

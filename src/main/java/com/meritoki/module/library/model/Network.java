package com.meritoki.module.library.model;

import java.util.concurrent.CountDownLatch;

public class Network extends Node {

	public static final int CONNECTION = 1;
	protected int tryMin = 0;
	protected int tryMax = 0;
	protected int timeout = -1;
	protected double connectionDelay;
	protected double acknowledgeDelay = 0.0D;
	protected String connection = "";
	protected boolean network = true;
	protected Input input = null;
	protected Output output = null;
	protected Delay delay = null;
	protected Protocol protocol = new Protocol();

	public Network() {
		super();

	}

	public Network(int id) {
		super(id);
	}

	public Network(Integer id, Module module) {
		super(id, module);
	}

	@Override
	public void initialize() {
		super.initialize();
		this.tryMax = Utility.stringToInteger(getProperty("@tryMax"));
		this.timeout = Utility.stringToInteger(getProperty("@timeout"));
		this.connection = getProperty("@connection", null);
		this.acknowledgeDelay = Utility.stringToDouble(getProperty("acknowledgeDelay"));
		this.connectionDelay = Utility.stringToDouble(getProperty("@connectionDelay"));
		logger.info("initialize() this.tryMax="+this.tryMax);
		logger.info("initialize() this.timeout="+this.timeout);
		logger.info("initialize() this.connection="+this.connection);
		logger.info("initialize() this.acknowledgeDelay="+this.acknowledgeDelay);
		logger.info("initialize() this.connectionDelay="+this.connectionDelay);
		this.stateMap.put(CONNECTION, "CONNECTION");
		this.setState(CONNECTION);
	}

	@Override
	protected void machine(int state, Object object) {
		switch (state) {
		case CONNECTION: {
			connectionState(object);
			break;
		}
		default:
			super.machine(state, object);
		}
	}

	@Override
	public void destroy() {
		if (!this.destroy) {
			super.destroy();
		}
	}

	protected void connectionState(Object object) {
		if ((object instanceof Data)) {
			Data data = (Data) object;
			switch (data.getType()) {
			case Data.POLL:
				poll(data, false);
			}
		}
		if (delayExpired()) {
			setDelay(newDelay(this.connectionDelay));
			if (getTry()) {
				if (connection()) {
					poll(null, true);
					setDelay(newDelay(this.inputDelay));
					setState(INPUT);
				}
			} else {
				this.destroy();
			}
		}
	}

	@Override
	protected void inputState(Object object) {
		if (input()) {
			if ((object instanceof Data)) {
//				logger.info("inputState("+object+")");
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
					break;
				case Data.POLL:
					poll(data, this.poll);
					break;
				}
			}
		} else {
			setState(CONNECTION);
		}
	}

	protected void output(Object object) {
		logger.info("output(" + object + ")");
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			if (this.delay != null) {
				if (protocol.getMessageOffset() < this.protocol.getMessageOffset()) {
					logger.warn(
							"output(" + object + ") ((protocol.getMessageOffset() = " + this.protocol.getMessageOffset()
									+ ") < (this.messegeOffset = " + protocol.getMessageOffset() + "))");
				} else if (protocol.getTryCount() > this.tryMax) {
					logger.warn("output(" + object + ") ((protocol.getTryCount() = " + protocol.getTryCount()
							+ ") > (this.MAX_TRIES = " + this.tryMax + "))");
					setState(DEFAULT);
				} else {
					this.output.add(new Data(this.id.intValue(), this.id.intValue(), 1, 0.0D, protocol, null));
					int protocolDataLength;
					if ((protocolDataLength = protocol.getDataLength()) > 0) {
						this.protocol.setMessageOffset(this.protocol.getMessageOffset() + protocolDataLength);
						protocol.setTryCount(protocol.getTryCount() + 1);
						this.delay.add(new Data(this.id.intValue(), this.id.intValue(), 1,
								protocol.getTimeout() * protocol.getTryCount(), protocol, this.objectList));
					}
				}
			} else {
				this.output.add(new Data(this.id.intValue(), this.id.intValue(), 1, 0.0D, protocol, null));
			}
		}
	}

	protected void input(Object object) {
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			switch (protocol.getType()) {
			case Protocol.ADVERTISEMENT:
				protocolSetMessageAcknowledged(object);
				break;
			case Protocol.MESSAGE:
				if (protocolSetMessageAcknowledged(object)) {
					delayAcknowledge(object);
				}
				outputProtocolAdvertisement();
				break;
			case Protocol.DISCONNECT:
				if (logger.isDebugEnabled()) {
					logger.debug("input(" + object + ") Protocol.DISCONNECT");
				}
				setState(DEFAULT);
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
				this.delay.add(new Data(this.id.intValue(), this.id.intValue(), 1233, this.acknowledgeDelay,
						Integer.valueOf(protocol.getMessageOffset() + protocol.getDataLength()), this.objectList));
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

	protected boolean input() {
		boolean input = true;
		if (this.moduleMap.size() < this.moduleMapSize) {
			logger.warn("input() (this.moduleMap.size()<this.moduleMapSize)");
			input = false;
		}
		return input;
	}

	protected boolean connection() {
		boolean connection = false;
		return connection;
	}

	protected boolean connection(Object object) {
		return this.connectionStart();
	}

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
}

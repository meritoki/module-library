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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import com.meritoki.library.controller.json.JsonController;
import com.meritoki.module.library.model.data.Data;
import com.meritoki.module.library.model.data.DataType;
import com.meritoki.module.library.model.io.Response;
import com.meritoki.module.library.model.protocol.Protocol;
import com.meritoki.module.library.model.protocol.ProtocolType;

public class Network extends Node {
	
	protected Logger logger = Logger.getLogger(Network.class.getName());
	protected int tryMin = 0;
	protected int tryMax = 0;
	protected int timeout = 5;
	protected double connectionDelay = 1.0;
	protected double acknowledgeDelay = 1.0;
	protected double aliveDelay = 10.0;
	protected String connection = null;
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
		this.tryMax = Utility.stringToInteger(getProperty("@tryMax", String.valueOf(this.tryMax)));
		this.timeout = Utility.stringToInteger(getProperty("@timeout", String.valueOf(this.timeout)));
		this.connection = getProperty("@connection");
		this.acknowledgeDelay = Utility.stringToDouble(getProperty("acknowledgeDelay",String.valueOf(this.acknowledgeDelay)));
		this.connectionDelay = Utility.stringToDouble(getProperty("@connectionDelay",String.valueOf(this.connectionDelay)));
		this.aliveDelay = Utility.stringToDouble(getProperty("@aliveDelay",String.valueOf(this.aliveDelay)));
		logger.fine("initialize() this.tryMax=" + this.tryMax);
		logger.fine("initialize() this.timeout=" + this.timeout);
		logger.fine("initialize() this.connection=" + this.connection);
		logger.fine("initialize() this.acknowledgeDelay=" + this.acknowledgeDelay);
		logger.fine("initialize() this.connectionDelay=" + this.connectionDelay);
	}

	@Override
	protected void machine(State state, Object object) {
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
			case POLL:
				poll(data, false);
			}
		}
		if (delayExpired()) {
			setDelay(newDelay(this.connectionDelay));
			if (getTry()) {
				if (connection()) {
					this.protocol = new Protocol();
					poll(null, true);
					setDelay(newDelay(this.inputDelay));
					setState(State.INPUT);
				}
			} else {
				setState(State.DEFAULT);
				this.destroy();
			}
		}
	}

	@Override
	protected void inputState(Object object) {
		if (io()) {
			if ((object instanceof Data)) {
				Data data = (Data) object;
				object = data.getObject();
				switch (data.getType()) {
				case OUTPUT: {
					output(object);
					break;
				}
				case INPUT: {
					input(object);
					break;
				}
				case ACKNOWLEDGE: {
					acknowledge(object);
					break;
				}
				case POLL: {
					poll(data, true);
					break;
				}
				default: {
					logger.warning("intputState("+object+") "+data.getType()+" Unsupported");
				}
				}
			}
			if(aliveExpired()) {
				setAlive(newAlive(this.aliveDelay));
				Response response = new Response();
				response.uuid = null;
				response.data = "true";
				Protocol protocol = new Protocol();
				protocol.serialize(ProtocolType.MESSAGE, this.protocol.getMessageOffset(),
						this.protocol.getMessageAcknowledged(), JsonController.getJson(response));
				this.output(protocol);
			}
		} else {
			setState(State.DEFAULT);
			this.destroy();
		}
	}

	protected void output(Object object) {
		logger.fine("output(" + object + ")");
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			if (this.delay != null) {
				if (protocol.getMessageOffset() < this.protocol.getMessageOffset()) {
					logger.warning(
							"output(" + object + ") (protocol.getMessageOffset() < this.protocol.getMessageOffset())");
				} else if (protocol.getTryCount() > this.tryMax) {
					logger.warning("output(" + object + ") (protocol.getTryCount() > this.tryMax)");
					setState(State.CONNECTION);
				} else {
					this.output.add(
							new Data(this.id.intValue(), this.id.intValue(), DataType.OUTPUT, 0.0D, protocol, null));
					int protocolDataLength;
					if ((protocolDataLength = protocol.getData().length()) > 0) {
						this.protocol.setMessageOffset(this.protocol.getMessageOffset() + protocolDataLength);
						protocol.setTryCount(protocol.getTryCount() + 1);
						this.delay.add(new Data(this.id.intValue(), this.id.intValue(), DataType.OUTPUT,
								protocol.getTimeout() * protocol.getTryCount(), protocol, this.objectList));
					}
				}
			} else {
				this.output
						.add(new Data(this.id.intValue(), this.id.intValue(), DataType.OUTPUT, 0.0D, protocol, null));
			}
		}
	}

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
			case DISCONNECT: {
				logger.info("input(" + object + ") Protocol.DISCONNECT");
				this.input.destroy();
				this.output.destroy();
				this.delay.destroy();
				this.input = null;
				this.output = null;
				this.delay = null;
				setState(State.CONNECTION);
				break;
			}
			default:
				break;
			}
		}
	}

	protected void acknowledge(Object object) {
		if (((object instanceof Integer)) && (shouldAcknowledge(((Integer) object).intValue()))) {
			outputProtocolAdvertisement();
		}
	}

	protected void protocol(Protocol protocol) {
		inputData(protocol.getData());
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
			if (protocol.getData().length() > 0) {
				protocol(protocol);
				this.delay.add(new Data(this.id.intValue(), this.id.intValue(), DataType.ACKNOWLEDGE,
						this.acknowledgeDelay, Integer.valueOf(protocol.getMessageOffset() + protocol.getData().length()),
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
				this.protocol.setMessageAcknowledged(this.protocol.getMessageAcknowledged() + protocol.getData().length());
				flag = true;
			}
		}
		return flag;
	}

	public void outputProtocolDisconnect() {
		Protocol protocol = new Protocol();
		protocol.serialize(ProtocolType.DISCONNECT, this.protocol.getMessageOffset(), this.protocol.getMessageAcknowledged(), "");
		Data data = new Data(this.id.intValue(), this.id.intValue(), DataType.OUTPUT, 0.0D, protocol, null);
		this.output.add(data);
	}

	protected void outputProtocolAdvertisement() {
		Protocol protocol = new Protocol();
		protocol.serialize(ProtocolType.ADVERTISEMENT, this.protocol.getMessageOffset(), this.protocol.getMessageAcknowledged(), "");
		this.output.add(new Data(this.id.intValue(), this.id.intValue(), DataType.OUTPUT, 0.0D, protocol, null));
	}

	public boolean getTry() {
		boolean flag = true;
		if (this.tryMax > 0) {
			if (this.tryMin < this.tryMax) {
				logger.fine("getTry() ((tryMin = " + this.tryMin + ") < (tryMax = " + this.tryMax + "))");
				this.tryMin += 1;
			} else {
				logger.warning("getTry(" + this.tryMin + ", " + this.tryMax + ") (tryMin>=tryMax)");
				flag = false;
			}
		}
		return flag;
	}

	protected boolean io() {
		boolean input = true;
		if (this.moduleMap.size() < this.moduleMapSize) {
			logger.warning("io() (this.moduleMap.size()<this.moduleMapSize)");
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
			logger.fine("connectionStart() (this.input == " + this.input + ")");
		}
		if (this.output != null) {
			count++;
			logger.fine("connectionStart() (this.output == " + this.output + ")");
		}
		if (this.delay != null) {
			count++;
			logger.fine("connectionStart() (this.delay == " + this.delay + ")");
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
			logger.fine("connectionStart() (countDownLatch.await())");
			countDownLatch.await();
			flag = true;
		} catch (InterruptedException ie) {
			logger.warning("connectionStart() InterruptedException");
		}
		return flag;
	}

	protected Input newInput(int id, Module module, InputStream inputStream) {
		return new Input(id, module, inputStream);
	}

	protected Output newOutput(int id, Module module, OutputStream outputStream) {
		return new Output(id, module, outputStream);
	}

	protected Delay newDelay(int id, Module module) {
		return new Delay(id, module);
	}
}

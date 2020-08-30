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

import com.meritoki.module.library.model.data.Data;
import com.meritoki.module.library.model.data.DataType;
import com.meritoki.module.library.model.protocol.Protocol;

public class Input extends Node {
	protected InputStream inputStream = null;
	protected int byteArrayLength = 0;
	protected byte[] byteArray = new byte[0];
	protected double minInputDelay = 2.0D;
	protected double maxInputDelay = 10.0D;

	public Input(int id, Module module, InputStream inputStream) {
		super(Integer.valueOf(id), module);
		this.inputStream = inputStream;
	}

	@Override
	public void initialize() {
		super.initialize();
		this.minInputDelay = Utility.stringToDouble(getProperty("@minInputDelay"));
		this.maxInputDelay = Utility.stringToDouble(getProperty("@maxInputDelay"));
		this.byteArrayLength = Utility.stringToInteger(getProperty("@byteArrayLength"));
		logger.info("initialize() this.minInputDelay="+this.minInputDelay);
		logger.info("initialize() this.maxInputDelay="+this.maxInputDelay);
		logger.info("initialize() this.byteArrayLength="+this.byteArrayLength);
		this.setState(State.INPUT);
	}

	@Override
	protected void inputState(Object object) {
		input(object);
	}

	@Override
	public void destroy() {
		if (!this.destroy) {
			super.destroy();
			inputStreamClose(this.inputStream);
		}
	}
	
	public void input(Object object) {
		try {
			if (this.inputStream.read(this.byteArray = new byte[this.byteArrayLength]) != -1) {
				setDelay(newDelay(this.minInputDelay));
				Protocol protocol = new Protocol();
				protocol.deserialize(this.byteArray);
				switch (protocol.getState()) {
				case GOOD: {
					inputData(protocol);
					break;
				}
				case BAD: {
					protocol = new Protocol();
					break;
				}
				default: {
					break;
				}
				}
			}
		} catch (IOException e) {
			logger.severe("input(object) IOException");
			this.destroy();
		}
	}

	protected void inputData(Object object) {
		if (object != null) {
			logger.finest("inputContainer(" + object + ")");
			this.rootAdd(new Data(this.id.intValue(), this.id.intValue(), DataType.INPUT, 0.0D, object, null));
		}
	}
	
	protected void inputStreamClose(InputStream inputStream) {
		logger.finest(this + ".inputStreamClose(" + inputStream + ")");
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.warning(this + ".inputStreamClose(" + inputStream + ") IOException");
			}
		} else {
			logger.warning(this + ".inputStreamClose(" + inputStream + ") (inputStream = " + inputStream + ")");
		}
	}
}

//if (delayExpired()) {
//switch (this.state) {
//case INPUT:
//	if (this.poll) {
//		this.poll = false;
//		inputData(Boolean.valueOf(this.poll));
//		setDelay(newDelay(this.waitForInputMaxDelay));
//		logger.warning("input() (this.setDelayExpiration(this.newDelayExpiration(" + this.waitForInputMaxDelay
//				+ ")))");
//	} else {
//		logger.warning("input() this.delayExpired()");
//		setState(0);
//	}
//	break;
//}
//}

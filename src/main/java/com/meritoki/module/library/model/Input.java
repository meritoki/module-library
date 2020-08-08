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

import java.io.IOException;
import java.io.InputStream;

public class Input extends Node {
	protected InputStream inputStream = null;
	protected int byteArrayLength = 0;
	protected byte[] byteArray = new byte[0];
	protected double waitForInputMinDelay = 2.0D;
	protected double waitForInputMaxDelay = 10.0D;

	public Input(int id, Module module, InputStream inputStream) {
		super(Integer.valueOf(id), module);
		this.inputStream = inputStream;
	}

	public void initialize() {
		super.initialize();
		this.waitForInputMinDelay = Utility.stringToDouble(getProperty("@waitForInputMinDelay"));
		this.waitForInputMaxDelay = Utility.stringToDouble(getProperty("@waitForInputMaxDelay"));
		this.byteArrayLength = Utility.stringToInteger(getProperty("@byteArrayLength"));
	}

	public void destroy() {
		if (!this.destroy) {
			super.destroy();
			inputStreamClose(this.inputStream);
		}
	}

	public void input(Object object) {
		try {
			if (this.inputStream.read(this.byteArray = new byte[this.byteArrayLength]) != -1) {
				setDelay(newDelay(this.waitForInputMinDelay));
				object = this.protocol.deserialize(this.byteArray);
				if ((object instanceof Protocol)) {
					this.protocol = ((Protocol) object);
					switch (this.protocol.getState()) {
					case 8:
						this.poll = true;
						inputData(this.protocol);
						break;
					case 9:
						this.protocol = new Protocol();
					}
				}
			}
		} catch (IOException e) {
			logger.fatal("input(object) IOException");
			setState(0);
		}
		if (delayExpired()) {
			switch (this.state) {
			case 2:
				if (this.poll) {
					this.poll = false;
					inputData(Boolean.valueOf(this.poll));
					setDelay(newDelay(this.waitForInputMaxDelay));
					logger.warn("input() (this.setDelayExpiration(this.newDelayExpiration(" + this.waitForInputMaxDelay
							+ ")))");
				} else {
					logger.warn("input() this.delayExpired()");
					setState(0);
				}
				break;
			}
		}
	}

	protected void inputState(Object object) {
		input(object);
	}

	protected void inputData(Object object) {
		if (object != null) {
			if (logger.isDebugEnabled()) {
				logger.trace("inputContainer(" + object + ")");
			}
			add(new Data(this.id.intValue(), this.id.intValue(), 2, 0.0D, object, null));
		}
	}
}

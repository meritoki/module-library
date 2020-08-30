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

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class Machine extends Module {
	
	protected Logger logger = Logger.getLogger(Machine.class.getName());
	protected State state = null;
	protected State previousState = null;
	protected double defaultDelay = 1.0;
	protected long sleepDelay = 100;

	public static void main(String[] args) {
		Machine stateMachine = new Machine(0);
		CountDownLatch countDownLatch;
	    stateMachine.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		stateMachine.start();
	}

	public Machine() {
	}

	public Machine(int id) {
		super(id);
	}

	public Machine(int id, Module module) {
		super(Integer.valueOf(id), module);
	}

	@Override
	public void initialize() {
		super.initialize();
		logger.info("initialize() this.defaultDelay="+this.defaultDelay);
	}

	public void run() {
		super.run();
		while (this.run) {
			machine();
			this.sleep(this.sleepDelay);
		}
	}

	public State getState() {
		return this.state;
	}

	protected void machine() {
		Object object = remove(0);
		machine(this.state, object);
		
	}

	protected void machine(State state, Object object) {
		switch (state) {
		case DEFAULT:
			defaultState(object);
			break;
		default:
			logger.warning("machine(...) NO STATE");
		}
	}
	
	protected void defaultState(Object object) {
		if (delayExpired()) {
			setDelay(newDelay(this.defaultDelay));
		}
	}

	protected Object test(int state, Object object) {
		return object;
	}

	protected void setState(State state) {
		logger.info(this + ".setState(" + state + ")");
		setState(state, false);
	}

	protected void setState(State state, boolean flag) {
		if (flag) {
			this.previousState = this.state;
		} else {
			this.previousState = null;
		}
		this.state = state;
	}
}

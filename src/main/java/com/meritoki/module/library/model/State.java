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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class State extends Module {
	public static final int DEFAULT = 0;
	protected Map<Integer, String> stateMap;
	protected int state = 0;
	protected Integer previousState = null;
	protected double defaultDelay = 1.0;

	public static void main(String[] args) {
		State stateMachine = new State(0);
		CountDownLatch countDownLatch;
	    stateMachine.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		stateMachine.start();
	}

	public State() {
	}

	public State(int id) {
		super(id);
	}

	public State(int id, Module module) {
		super(Integer.valueOf(id), module);
	}

	@Override
	public void initialize() {
		super.initialize();
		this.stateMap = Collections.synchronizedMap(new ConcurrentHashMap());
		logger.info("initialize() this.defaultDelay="+this.defaultDelay);
		this.stateMap.put(Integer.valueOf(0), "DEFAULT");
		this.setState(DEFAULT);
	}

	public void run() {
		super.run();
		while (this.run) {
			machine();
		}
	}

	public int getState() {
		return this.state;
	}

	public String getState(int state) {
		return (String) this.stateMap.get(Integer.valueOf(state));
	}

	protected void machine() {
		Object object = remove(0);
		machine(this.state, object);
	}

	protected void machine(int state, Object object) {
		switch (state) {
		case DEFAULT:
			defaultState(object);
			break;
		default:
			logger.fine("machine(...) NO STATE");
		}
	}
	
	protected void defaultState(Object object) {
		if (delayExpired()) {
			logger.info("defaultState("+object+")");
			setDelay(newDelay(this.defaultDelay));
		}
	}

	protected Object test(int state, Object object) {
		return object;
	}

	protected void setState(int state) {
		logger.info(this + ".setState(" + getState(state) + ")");
		setState(state, false);
	}

	protected void setState(int state, boolean flag) {
		if (flag) {
			this.previousState = Integer.valueOf(this.state);
		} else {
			this.previousState = null;
		}
		this.state = state;
	}
}

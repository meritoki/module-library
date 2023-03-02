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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Module extends URLClassLoader implements ModuleInterface {
	protected Logger logger = LoggerFactory.getLogger(Module.class.getName());
	protected List<Object> objectList = Collections.synchronizedList(new ArrayList<>());
	protected Set<Integer> idSet = Collections.synchronizedSet(new HashSet<>());
	protected Map<String, Module> moduleMap = Collections.synchronizedMap(new LinkedHashMap<>());
	protected int moduleMapSize = 0;
	public Thread thread = null;
	public Module root = null;
	protected Integer id = Integer.valueOf(0);
	protected volatile boolean start = true;
	protected volatile boolean run = true;
	protected volatile boolean destroy = false;
	protected volatile boolean protect = false;
	protected double now = 0.0D;
	protected double delay = 0.0D;
	protected double alive = 0.0D;
	public boolean interrupt = true;
	protected CountDownLatch countDownLatch = null;

	public static void main(String[] args) {
		Module module = new Module(0);
		CountDownLatch countDownLatch;
		module.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		module.start();
	}

	public Module() {
		super(new URL[0], Module.class.getClassLoader());
		setID(0);
	}

	public Module(int id) {
		super(new URL[0], Module.class.getClassLoader());
		setID(id);
	}

	public Module(URL[] urlArray) {
		super(urlArray, Module.class.getClassLoader());
		setID(0);
	}

	public Module(int id, Module module) {
		super(module.getURLs(), Module.class.getClassLoader());
		setID(id);
		setRoot(module);
		if (this.root != null) {
			this.root.moduleMapPut(this);
		}
	}

	public Module(Module module) {
		super(module.getURLs(), Module.class.getClassLoader());
		setID(0);
		setRoot(module);
		if (this.root != null) {
			this.root.moduleMapPut(this);
		}
	}

	private void setID(int id) {
		this.id = Integer.valueOf(id);
		this.idSet.add(this.id);
	}


	public void start() {
		if (this.start) {
			this.start = false;
			this.run = true;
			logger.debug("start()");
			this.thread = new Thread(this);
			this.thread.setName(toString());
			this.thread.start();
		}
	}

	public void initialize() {
		logger.trace(this + ".initialize()");
	}

	public void run() {
		this.initialize();
		logger.trace(this + ".run()");
		this.countDownLatchCountDown();
	}

	public void stop() {
		logger.info(this + ".stop()");
		this.run = false;
		if ((this.thread != null)) {// && (this.interrupt)) {
			this.thread.interrupt();
			logger.info(this + ".stop() this.thread.isInterrupted()="+this.thread.isInterrupted());
			logger.info(this + ".stop() this.thread.isInterrupted()="+this.thread.isInterrupted());
		}
		this.start = true;
	}

	public void destroy() {
		if (!this.destroy) {
			stop();
			logger.info("destroy()");
			this.destroy = true;
			moduleMapDestroy(this.moduleMap);
			if (this.root != null) {
				this.root.moduleMapRemove(this);
			}
		}
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		logger.debug(this + ".setCountDownLatch(" + countDownLatch.getCount() + ")");
		this.countDownLatch = countDownLatch;
	}

	public void countDownLatchCountDown() {
		if (this.countDownLatch != null) {
			logger.debug("countDownLatchCountDown() (this.countDownLatch.getCount() = " + this.countDownLatch.getCount()
					+ ")");

			this.countDownLatch.countDown();

			logger.debug("countDownLatchCountDown() (this.countDownLatch.getCount() = " + this.countDownLatch.getCount()
					+ ")");

		} 
//		else {
//			logger.warn("countDownLatchCountDown() (this.countDownLatch == null)");
//		}
	}

	public boolean getStart() {
		return this.start;
	}

	public boolean getRun() {
		return this.run;
	}

	public boolean getProtect() {
		return this.protect;
	}

	public boolean getDestroy() {
		return this.destroy;
	}

	public int getID() {
		return this.id.intValue();
	}

	public void add(Object object) {
//		logger.info("add(" + object + ")");
		synchronized (this.objectList) {
			if ((object instanceof List)) {
				this.objectList.addAll((List) object);
			} else {
				this.objectList.add(object);
			}
			this.objectList.notify();
		}
	}

	public Object remove(int index) {
		Object object = null;
		synchronized (this.objectList) {
			if (index < this.objectList.size()) {
				try {
					object = this.objectList.remove(index);
				} catch (NoSuchElementException e) {
					logger.warn("remove(" + index + ") NoSuchElementException");
				}
			}
			this.objectList.notify();
		}
		return object;
	}

	public boolean removeAll() {
		boolean delta = false;
		synchronized (this.objectList) {
			delta = this.objectList.removeAll(this.objectList);
			this.objectList.notify();
		}
		return delta;
	}

	public void rootAdd(Object object) {
		if (this.root != null) {
			this.root.add(object);
		}
	}

	public Object load(Integer id, Object object) {
		return null;
	}

	public void moduleMapPut(Object object) {
		if ((object instanceof Module)) {
			Module module = (Module)object;
			module.setRoot(this);
			this.moduleMap.put(module.toString(), module);
		}
	}

	public void moduleMapRemove(Object object) {
		if ((object instanceof Module)) {
			this.moduleMap.remove(((Module) object).toString());
		}
	}

	public boolean moduleMapContains(Object object) {
		boolean flag = false;
		if ((object instanceof Module)) {
			flag = this.moduleMap.containsKey(((Module) object).toString());
		}
		return flag;
	}

	public void setRoot(Module root) {
		this.root = root;
	}

	public Class<?> getURLClass(String className) {
		Class<?> clazz = null;
		try {
			clazz = loadClass(className);
		} catch (NoClassDefFoundError e) {
			logger.warn("getURLClass(" + className + ") NoClassDefFoundError");
		} catch (ClassNotFoundException e) {
			logger.warn("getURLClass(" + className + ") ClassNotFoundException");
		} catch (SecurityException e) {
			logger.warn("getURLClass(" + className + ") SecurityException");
		}
		return clazz;
	}

	public List<Object> getInputObjectList() {
		return this.objectList;
	}

	public Module getRoot() {
		return this.root;
	}
	
	public Module getAbsoluteRoot() {
		if(this.root == null) {
			return this;
		}
		return this.root.getAbsoluteRoot();
	}

	public Map<String, Module> getModuleMap() {
		return this.moduleMap;
	}

	public Set<Integer> getIDSet() {
		return this.idSet;
	}

	public String toString() {
		String string = super.toString();
		String stringPackage = getClass().getPackage().getName();
		if (stringPackage != null) {
			string = string.replaceFirst("^" + stringPackage + ".", "");
			string = string.substring(0, string.indexOf('@'));
		}
		return string;
	}

	protected boolean delayExpired() {
		boolean flag = now() > this.delay;
		return flag;
	}

	protected boolean delayExpired(double delay) {
		boolean flag = now() > delay;
		return flag;
	}

	protected void setDelay(double delay) {
		this.delay = delay;
	}
	
	protected boolean aliveExpired() {
		boolean flag = now() > this.alive;
		return flag;
	}

	protected boolean aliveExpired(double delay) {
		boolean flag = now() > delay;
		return flag;
	}

	protected void setAlive(double delay) {
		this.alive = delay;
	}

	protected void moduleMapAdd(Map<String, Module> moduleMap, Object object) {
		if ((moduleMap != null) && (object != null)) {
			Set<String> moduleHashMapKeySet = moduleMap.keySet();
			Iterator<String> moduleHashMapIterator = moduleHashMapKeySet.iterator();
			while (moduleHashMapIterator.hasNext()) {
				String string;
				Module module;
				if (((string = (String) moduleHashMapIterator.next()) != null)
						&& ((module = (Module) moduleMap.get(string)) != null) && (!module.getDestroy())) {
					module.add(object);
				}
			}
		}
	}

	protected void moduleMapStart(Map<String, Module> moduleMap) {
//		logger.info("moduleMapStart(("+moduleMap+")");
		Set<String> moduleMapKeySet = moduleMap.keySet();
		Iterator<String> moduleHashMapIterator = moduleMapKeySet.iterator();
		while (moduleHashMapIterator.hasNext()) {
			String string;
			if ((string = (String) moduleHashMapIterator.next()) != null) {
				Module module;
				if (((module = (Module) moduleMap.get(string)) != null) && (module.getStart())) {
					module.start();
				}
			}
		}
	}

	protected void moduleMapDestroy(Map<String, Module> moduleMap) {
		if ((moduleMap != null) && (!moduleMap.isEmpty())) {
			Set<String> moduleHashMapKeySet = moduleMap.keySet();
			Iterator<String> moduleHashMapIterator = moduleHashMapKeySet.iterator();
			while (moduleHashMapIterator.hasNext()) {
				String string;
				Module module;
				if (((string = (String) moduleHashMapIterator.next()) != null)
						&& ((module = (Module) moduleMap.get(string)) != null) && (!module.getDestroy())) {
					module.destroy();
				}
			}
		}
	}

	protected Set<Integer> moduleMapGetDestroy(Map<String, Module> moduleMap, Set<Integer> idSet) {
		Set<Integer> loadIDSet = Collections.synchronizedSet(new HashSet<>());
		if ((moduleMap != null) && (idSet != null)) {
			loadIDSet.addAll(idSet);
			loadIDSet.remove(this.id);
			if (!moduleMap.isEmpty()) {
				Iterator<String> moduleMapKeySetIterator = moduleMap.keySet().iterator();
				while (moduleMapKeySetIterator.hasNext()) {
					String string;
					Module module;
					if (((string = (String) moduleMapKeySetIterator.next()) != null)
							&& ((module = (Module) moduleMap.get(string)) != null)) {
						if (!module.getDestroy()) {
							loadIDSet.remove(Integer.valueOf(module.getID()));
						} else {
							moduleMap.remove(string);
							moduleMapKeySetIterator.remove();
						}
					}
				}
			}
		}
		return loadIDSet;
	}

	protected boolean moduleMapGetProtect(Map<String, Module> moduleMap) {
		Set<String> moduleHashMapKeySet = moduleMap.keySet();
		Iterator<String> moduleHashMapIterator = moduleHashMapKeySet.iterator();

		boolean flag = false;
		while (moduleHashMapIterator.hasNext()) {
			String string;
			Module module;
			if (((string = (String) moduleHashMapIterator.next()) != null)
					&& ((module = (Module) moduleMap.get(string)) != null) && (module.getProtect())) {
				flag = true;
			}
		}
		return flag;
	}

	protected Set<Integer> moduleMapGetIDSet(Map<String, Module> moduleMap) {
		Set<Integer> idSet = Collections.synchronizedSet(new HashSet<>());
		if ((moduleMap != null) && (!moduleMap.isEmpty())) {
			Set<String> moduleMapKeySet = moduleMap.keySet();
			Iterator<String> moduleMapKeySetIterator = moduleMapKeySet.iterator();
			while (moduleMapKeySetIterator.hasNext()) {
				String string;
				Module module;
				if (((string = (String) moduleMapKeySetIterator.next()) != null)
						&& ((module = (Module) moduleMap.get(string)) != null) && (!module.getDestroy())) {
					idSet.add(Integer.valueOf(module.getID()));
				}
			}
		}
		return idSet;
	}

	protected double newDelay(double delay) {
		logger.trace("newDelay(" + delay + ")");
		Date date = new Date();
		double time = date.getTime();
		double now = time / 1000.0D;
		return now + delay;
	}
	
	protected double newAlive(double delay) {
		logger.trace("newAlive(" + delay + ")");
		Date date = new Date();
		double time = date.getTime();
		double now = time / 1000.0D;
		return now + delay;
	}

	protected double now() {
		Date nowDate = new Date(System.currentTimeMillis());
		double nowDateDouble = nowDate.getTime();
		double now = nowDateDouble / 1000.0D;
		return now;
	}

	protected double newDate(Date date) {
		double dateDouble = date.getTime();
		return dateDouble / 1000.0D;
	}

	protected void sleep(long milliseconds) {
		logger.trace("sleep(" + milliseconds + ")");
		if (milliseconds > 0) {
			try {
				Thread.sleep(milliseconds);
			} catch (InterruptedException e) {
				logger.warn("sleep(" + milliseconds + ") InterruptedException");
			}
		}
	}

	public int getModuleMapSize() {
		return this.moduleMap.size();
	}
}

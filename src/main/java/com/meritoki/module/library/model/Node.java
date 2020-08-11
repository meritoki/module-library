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

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;

import com.meritoki.library.controller.node.NodeController;
import com.meritoki.module.library.model.data.Data;
import com.meritoki.module.library.model.data.DataType;

public class Node extends State {
	public static final int INPUT = 2;
	protected double inputDelay;
	protected Properties idProperties = null;
	protected Set<Object> idPropertiesKeySet = new HashSet<>();
	protected String configurationPropertiesPath = null;
	protected Properties configurationProperties = null;
	protected Set<Object> configurationPropertiesKeySet = new HashSet<>();
	protected boolean filter = true;
	
	public static void main(String[] args) {
		Node node = new Node(0);
		CountDownLatch countDownLatch;
		node.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		node.start();
		node.add(new Data(0, 0, DataType.POLL, 0, true, node.objectList));
	}

	public Node() {
	}

	public Node(int id) {
		super(id);
	}

	public Node(Integer id, Module module) {
		super(id.intValue(), module);
	}

	@Override
	public void initialize() {
		super.initialize();
		this.idProperties = idPropertiesLoadFromXML(this.id.intValue());
		this.configurationProperties = configurationPropertiesLoadFromXML(this.idProperties);
		this.idSet.addAll(Utility.stringToIntegerSet(getProperty("idSet"), ",", "-"));
		this.inputDelay = Utility.stringToDouble(getProperty("@inputDelay"));
		logger.info("initialize() this.idSet="+this.idSet);
		logger.info("initialize() this.inputDelay="+this.inputDelay);
		this.stateMap.put(INPUT, "INPUT");
		this.setState(INPUT);
	}

	public void destroy() {
		if (!this.destroy) {
			this.interrupt = false;
			super.destroy();
			if (this.thread != null) {
				this.thread.interrupt();
			}
		}
	}

	@Override
	public void add(Object object) {
		logger.info("add("+object+")");
		if (this.filter) {
			if ((object instanceof Data)) {
				Data data = (Data) object;
				if (data.getDestinationID() == this.id) {
					logger.fine("add(" + object + ") (data.getDestinationID() == this.id)");
					if (this.idSet.contains(data.getSourceID())) {
						logger.fine("add(" + object + ") (this.idSet.contains(data.getSourceID()))");
						super.add(data);
					} else {
						logger.warning("add(" + object + ") !(this.idSet.contains(data.getSourceID()))");
					}
				}
			}
		} else {
			super.add(object);
		}
	}

	public Properties getConfigurationProperties() {
		return this.configurationProperties;
	}

	@Override
	protected void machine(int state, Object object) {
		switch (state) {
		case INPUT: {
			inputState(object);
			break;
		}
		default:
			super.machine(state, object);
		}
	}

	protected void inputState(Object object) {
		if ((object instanceof Data)) {
			Data data = (Data) object;
			object = data.getObject();
			switch (data.getType()) {
			case POLL:
				poll(data, true);
			}
		}
	}

	protected void poll(Object object, boolean flag) {
		logger.info("poll(" + object + ", " + flag + ")");
		if ((object instanceof Data)) {
			Data data = (Data) object;
			int sourceID = data.getSourceID();
			if ((sourceID != this.id.intValue()) && (!data.objectListAdd(
					new Data(sourceID, this.id.intValue(), DataType.INPUT, 0.0D, Boolean.valueOf(flag), null)))) {
				data = null;
			}
		} else {
			//sends true to all listening and filtered modules
			inputData(flag);
		}
	}

	protected void inputData(Object object) {
		if (object != null) {
			logger.finest("inputContainer(" + object + ")");
			Iterator<Integer> idSetIterator = this.idSet.iterator();
			Integer id = null;
			while (idSetIterator.hasNext()) {
				id = (Integer) idSetIterator.next();
				if (this.id != id) {
					add(new Data(id.intValue(), this.id.intValue(), DataType.INPUT, 0.0D, object, null));
				}
			}
		}
	}

	protected Properties idPropertiesLoadFromXML(int id) {
		logger.info("idPropertiesLoadFromXML(" + id + ")");
		Properties properties = NodeController.openPropertiesXML(getClass().getResourceAsStream(id + ".xml"));
		if (properties == null) {
			properties = new Properties();
		} else {
			this.idPropertiesKeySet = properties.keySet();
		}
		return properties;
	}

	protected Properties configurationPropertiesLoadFromXML(Properties properties) {
		logger.fine("configurationPropertiesLoadFromXML(" + properties + ")");
		Properties configurationProperties = new Properties();
		if (properties != null) {
			this.configurationPropertiesPath = properties.getProperty("configurationPropertiesPath");
			logger.finest("configurationPropertiesLoadFromXML(properties) (this.configurationPropertiesPath = "
					+ this.configurationPropertiesPath + ")");
			if (StringUtils.isNotBlank(this.configurationPropertiesPath)) {
				File configurationPropertiesFile = new File(this.configurationPropertiesPath);
				if (!configurationPropertiesFile.exists()) {
					logger.finest(
							"configurationPropertiesLoadFromXML(properties) (!configurationPropertiesFile.exists())");
					NodeController.savePropertiesXML(configurationProperties, this.configurationPropertiesPath, "");
				} else {
					logger.finest(
							"configurationPropertiesLoadFromXML(properties) (configurationPropertiesFile.exists())");
					configurationProperties = NodeController.openPropertiesXML(configurationPropertiesFile);
					this.configurationPropertiesKeySet = configurationProperties.keySet();
				}
			} else if ((this.root instanceof Node)) {
				Node node = (Node) this.root;
				if (node.getConfigurationProperties() != null) {
					configurationProperties = node.getConfigurationProperties();
					this.configurationPropertiesKeySet = configurationProperties.keySet();
					this.configurationPropertiesPath = node.configurationPropertiesPath;
				} else {
					configurationProperties = node.configurationPropertiesLoadFromXML(node.getIDProperties());
				}
			}
		}
		return configurationProperties;
	}

	public Properties getIDProperties() {
		return this.idProperties;
	}

	protected String getIDProperty(String key) {
		return getProperty(this.idProperties, key);
	}

	protected String getConfigurationProperty(String key) {
		return getProperty(this.configurationProperties, key);
	}

	protected String getProperty(String key) {
		return getProperty(key, null);
	}

	protected String getProperty(String key, String defaultValue) {
		String property = null;
		if (StringUtils.isNotBlank(key)) {
			if (this.idPropertiesKeySet.contains(key)) {
				property = getIDProperty(key);
			} else if ((this.configurationPropertiesKeySet != null)
					&& (this.configurationPropertiesKeySet.contains(key))) {
				property = getConfigurationProperty(key);
			} else {
				String keyDelta = null;
				String keyClassName = Utility.getKeyClassName(getClass());
				String keyAttributeName = null;
				switch (key.charAt(0)) {
				case '@':
					key = key.substring(1, key.length());
					if ((this.idPropertiesKeySet != null) && (this.idPropertiesKeySet.contains(key))) {
						keyAttributeName = Utility.getKeyAttributeName(key);
						keyDelta = keyClassName + keyAttributeName;
					}
					break;
				case '#':
					key = key.substring(1, key.length());
					if ((this.idPropertiesKeySet != null) && (this.idPropertiesKeySet.contains(key))) {
						keyAttributeName = Utility.getKeyAttributeName(key);
						keyDelta = keyClassName + this.id + keyAttributeName;
					}
					break;
				}
				if (keyDelta != null) {
					if ((this.configurationPropertiesKeySet != null)
							&& (this.configurationPropertiesKeySet.contains(keyDelta))) {

						logger.finest("getProperty(" + key + ", " + defaultValue
								+ ") (this.configurationPropertiesKeySet.contains(" + keyDelta + "))");

						property = getConfigurationProperty(keyDelta);
					} else {

						logger.finest("getProperty(" + key + ", " + defaultValue
								+ ") (!this.configurationPropertiesKeySet.contains(" + keyDelta + "))");

						String value = getIDProperty(key);
						setConfigurationProperty(keyDelta, value);
						property = value;
					}
				}
			}
		}
		if (property == null) {
			property = defaultValue;
		}
		logger.finest("getProperty(" + key + ", " + defaultValue + ") (" + key + ", " + property + ")");
		return property;
	}

	protected String getProperty(Properties properties, String key) {
		return properties.getProperty(key);
	}

	protected boolean setConfigurationProperty(String key, String valueBeta) {
		return setConfigurationProperty(this.configurationProperties, this.configurationPropertiesPath, key, valueBeta);
	}

	protected boolean setConfigurationProperty(Properties properties, String propertiesPath, String key, String value) {
		logger.finest("setConfigurationProperty(properties, " + propertiesPath + ", " + key + ", " + value + ")");
		String valueAlpha = properties.getProperty(key);
		boolean flag = false;
		String date = Utility.formatDate("GMT", "yyyyMMddHHmmss", new Date());
		if (value != null) {
			if (valueAlpha != null) {
				if (!value.equals(valueAlpha)) {
					properties.setProperty(key, value);
					flag = NodeController.savePropertiesXML(properties, propertiesPath, date);
				}
			} else {
				properties.setProperty(key, value);
				flag = NodeController.savePropertiesXML(properties, propertiesPath, date);
			}
		} else if (properties.containsKey(key)) {
			properties.remove(key);
			flag = NodeController.savePropertiesXML(properties, propertiesPath, date);
		}
		return flag;
	}

	protected String newFileURL(String path, String fileName) {
		String fileURL = "";
		if ((!StringUtils.isBlank(path)) && (!StringUtils.isBlank(fileName))) {
			fileURL = "file:" + path + "/" + fileName;
		}
		return fileURL;
	}
}

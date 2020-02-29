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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.management.MBeanServer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class Node extends StateMachine implements NodeMBean {
	public static final int LINUX = 1;
	public static final int WINDOWS = 2;
	public static final int MAC = 3;
	public static final int INPUT = 2;

	protected double connectionDelay;
	protected double inputDelay;
	protected Protocol protocol = new Protocol();
	protected Runtime runtime;
	protected Process process;

	protected Properties idProperties = null;
	protected Set<Object> idPropertiesKeySet = new HashSet();
	protected String configurationPropertiesPath = null;
	protected Properties configurationProperties = null;
	protected Set<Object> configurationPropertiesKeySet = new HashSet();
	protected String log4JPath = null;
	protected int operatingSystem = 0;
	protected boolean log4JEncrypt;
	protected String connection = "";
	protected int tryMin = 0;
	protected int tryMax = 0;
	protected int timeout = -1;
	private boolean data = true;
	protected boolean poll = false;
	protected boolean add = true;
	protected double acknowledgeDelay = 0.0D;

	public static void main(String[] args) {

		Node node = new Node(0);
		CountDownLatch countDownLatch;
		node.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		node.start();
	}

	public Node() {
	}

	public Node(int id) {
		super(id);
	}

	public Node(URL[] urlArray, MBeanServer mBeanServer) {
		super(urlArray, mBeanServer);
	}

	public Node(Integer id, Module module) {
		super(id.intValue(), module);
	}

	@Override
	public void initialize() {
		super.initialize();
		this.idProperties = idPropertiesLoadFromXML(this.id.intValue());
		this.configurationProperties = configurationPropertiesLoadFromXML(this.idProperties);
//		this.log4JPath = getProperty("@log4JPath");
//		logger.info(this.log4JPath);
//		if (StringUtils.isNotBlank(this.log4JPath)) {
//			System.out.println("log4j not null");
//			File log4JFile = new File(FilenameUtils.normalize(this.log4JPath));
//			if (log4JFile.exists()) {
//				System.out.println("log4j exists");
////			    System.setProperty("log4j2.configurationFile", log4JFile.toURI().toString());
////				ConfigurationSource source;
////				try {
////					source = new ConfigurationSource(new FileInputStream(log4JFile));
////					Configurator.initialize(null, source);
////					logger = LogManager.getLogger(getClass());
////				} catch (FileNotFoundException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				} catch (IOException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
//		
//			} else {
//				byte[] log4JByteArray = readFile(getClass().getResourceAsStream(log4JFile.getName()));
//				if (log4JByteArray.length > 0) {
//					if (!writeFile(log4JFile, log4JByteArray)) {
//						log4JFile.delete();
//					}
//				} else {
//					logger.warn("initialize() (log4JByteArray.length = 0)");
//				}
//			}
//		} else {
//			logger = LogManager.getLogger(getClass());
//		}
		this.idSet.addAll(Utility.stringToIntegerSet(getProperty("idSet"), ",", "-"));
		this.stateMap.put(INPUT, "INPUT");
		this.inputDelay = Utility.stringToDouble(getProperty("@inputDelay"));
		this.runtime = Runtime.getRuntime();
		this.tryMax = Utility.stringToInteger(getProperty("@tryMax"));
		this.operatingSystem = newOperatingSystem();

	}

	public void destroy() {
		if (!this.destroy) {
			this.interrupt = false;
			super.destroy();
			if (this.process != null) {
				this.process.destroy();
			}
			if (this.thread != null) {
				this.thread.interrupt();
			}
		}
	}

	
	@Override
	public void add(Object object) {
//		logger.info("C add("+object+")");
//		if (this.add) {
//			if (this.data) {
				if ((object instanceof Data)) {
//					logger.info("D add("+object+")");
					Data data = (Data) object;
//					logger.info("D add("+object+") data.getDestinationID()="+data.getDestinationID());
//					logger.info("D add("+object+") this.id="+this.id);
					if (data.getDestinationID() == this.id.intValue()) {
//						if (logger.isDebugEnabled()) {
//							logger.info("add(" + object + ") ((data.getDestinationID()==this.id)");
//						}
						if (this.idSet.contains(Integer.valueOf(data.getSourceID()))) {
//							if (logger.isDebugEnabled()) {
//								logger.info("add(" + object
//										+ ") (this.idSet.contains(data.getSourceID()))");
//							}
							super.add(data);
						}
					}
				}
	}
//	@Override
//	public void add(Object object) {
////		if (this.add) {
////			if (this.data) {
//				if ((object instanceof Data)) {
//					logger.info("add("+object+")");
//					Data data = (Data) object;
//					if (data.getDestinationID() == this.id.intValue()) {
////						if (logger.isDebugEnabled()) {
//							logger.info("add(" + object + ") ((data.getDestinationID()==this.id)");
////						}
//						if (this.idSet.contains(Integer.valueOf(data.getSourceID()))) {
////							if (logger.isDebugEnabled()) {
//								logger.info("add(" + object
//										+ ") (this.idSet.contains(data.getSourceID()))");
////							}
//							super.add(data);
//						}
//					}
//				}
////			} else {
////				super.add(object);
////			}
////		}
//	}

	public String getConfigurationPropertiesPath() {
		if (logger.isDebugEnabled()) {
			logger.trace("getConfigurationPropertiesPath() (this.configurationPropertiesPath = "
					+ this.configurationPropertiesPath + ")");
		}
		return this.configurationPropertiesPath;
	}

	public Properties getConfigurationProperties() {
		return this.configurationProperties;
	}

	@Override
	protected void machine(int state, Object object) {
		switch (state) {
		case INPUT:
			inputState(object);
			break;
		default:
			super.machine(state, object);
		}
	}

	protected void inputState(Object object) {
		if ((object instanceof Data)) {
			Data data = (Data) object;
			object = data.getObject();
			switch (data.getType()) {
			case Data.POLL:
				poll(data, this.poll);
			}
		}
	}

	protected void poll(Object object, boolean flag) {
		if (logger.isDebugEnabled()) {
			logger.debug("poll(" + object + ", " + flag + ")");
		}
		if ((object instanceof Data)) {
			Data data = (Data) object;
			int sourceID = data.getSourceID();
			if ((sourceID != this.id.intValue()) && (!data.outputObjectListAdd(
					new Data(sourceID, this.id.intValue(), 2, 0.0D, Boolean.valueOf(flag), null)))) {
				data = null;
			}
		} else {
			inputData(Boolean.valueOf(flag));
		}
	}

	protected void inputData(Object object) {
		if (object != null) {
			if (logger.isDebugEnabled()) {
				logger.trace("inputContainer(" + object + ")");
			}
			Iterator<Integer> idSetIterator = this.idSet.iterator();
			Integer id = null;
			while (idSetIterator.hasNext()) {
				id = (Integer) idSetIterator.next();
				if (this.id != id) {
					add(new Data(id.intValue(), this.id.intValue(), 2, 0.0D, object, null));
				}
			}
		}
	}

	protected Properties idPropertiesLoadFromXML(int id) {
		logger.debug("idPropertiesLoadFromXML(" + id + ")");
		Properties properties = propertiesLoadFromXML(getClass().getResourceAsStream(id + ".xml"));
		if (properties == null) {
			properties = new Properties();
		} else {
			this.idPropertiesKeySet = properties.keySet();
		}
		return properties;
	}

	protected Properties configurationPropertiesLoadFromXML(Properties properties) {
		logger.debug("configurationPropertiesLoadFromXML(" + properties + ")");
		Properties configurationProperties = new Properties();
		if (properties != null) {
			this.configurationPropertiesPath = properties.getProperty("configurationPropertiesPath");
			logger.debug("configurationPropertiesLoadFromXML(properties) (this.configurationPropertiesPath = "
					+ this.configurationPropertiesPath + ")");
			if (StringUtils.isNotBlank(this.configurationPropertiesPath)) {
				File configurationPropertiesFile = new File(this.configurationPropertiesPath);
				if (!configurationPropertiesFile.exists()) {
					logger.debug(
							"configurationPropertiesLoadFromXML(properties) (!configurationPropertiesFile.exists())");
					propertiesStoreToXML(configurationProperties, this.configurationPropertiesPath, "");
				} else {
					logger.debug(
							"configurationPropertiesLoadFromXML(properties) (configurationPropertiesFile.exists())");
					configurationProperties = propertiesLoadFromXML(configurationPropertiesFile);
					this.configurationPropertiesKeySet = configurationProperties.keySet();
				}
			} else if ((this.root instanceof Node)) {
				Node node = (Node) this.root;
				if (node.getConfigurationProperties() != null) {
					configurationProperties = node.getConfigurationProperties();
					this.configurationPropertiesKeySet = configurationProperties.keySet();
					this.configurationPropertiesPath = node.getConfigurationPropertiesPath();
				} else {
					configurationProperties = node.configurationPropertiesLoadFromXML(node.getIDProperties());
				}
			}
		}
		return configurationProperties;
	}

	protected int newOperatingSystem() {
		String osName = System.getProperty("os.name");
		int operatingSystem = -1;
		if (StringUtils.isNotBlank(osName)) {
			osName = osName.toLowerCase();
			if (osName.startsWith("linux")) {
				if (logger.isDebugEnabled()) {
					logger.debug("newOperatingSystem() Node.LINUX");
				}
				operatingSystem = 1;
			} else if (osName.startsWith("windows")) {
				if (logger.isDebugEnabled()) {
					logger.debug("newOperatingSystem() Node.WINDOWS");
				}
				operatingSystem = 2;
			} else {
				logger.warn("newOperatingSystem() (osName = " + osName + ")");
			}
		} else {
			logger.error("newOperatingSystem() (StringUtils.isNotBlank(osName)==false)");
		}
		return operatingSystem;
	}

	public String getHostAddress() {
		String hostAddress = null;
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.warn("getHostAddress() UnknownHostException");
		}
		return hostAddress;
	}

	public String getHostName() {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warn("getHostName() UnknownHostException");
		}
		return hostName;
	}

	public Properties getIDProperties() {
		return this.idProperties;
	}

	public boolean newDirectory(String directoryPath) {
		return newDirectory(new File(directoryPath));
	}

	public boolean newDirectory(File directory) {
		logger.info("newDirectory(" + directory + ")");
		boolean success = false;
		File parentDirectory = directory.getParentFile();
		if (!directory.exists()) {
			if (!parentDirectory.exists()) {
				parentDirectory.mkdirs();
			}
			success = directory.mkdir();
		}
		return success;
	}

	public boolean newFile(String fileName) {
		boolean flag = false;
		if (StringUtils.isNotBlank(fileName)) {
			flag = newFile(new File(fileName));
		}
		return flag;
	}

	public boolean newFile(File file) {
		if (logger.isDebugEnabled()) {
			logger.trace("newFile(" + file + ")");
		}
		boolean success = false;
		String newFileAbsolutePath = FilenameUtils.normalize(file.getAbsolutePath());
		File newFile = new File(newFileAbsolutePath);
		if (!newFile.exists()) {
			File parentDirectory = newFile.getParentFile();
			if ((parentDirectory != null) && (!parentDirectory.exists())) {
				parentDirectory.mkdirs();
			}
			try {
				success = newFile.createNewFile();
			} catch (IOException e) {
				logger.error("newFile(" + file + ") IOException");
			}
		} else {
			success = true;
		}
		return success;
	}

	public void copyDirectory(File sourceDirectory, File destinationDirectory) {
		logger.info("copyDirectory(" + sourceDirectory + ", " + destinationDirectory + ")");
		File[] fileArray = getDirectoryFileArray(sourceDirectory);
		File file = null;
		String fileName = null;
		String destinationDirectoryAbsolutePath = null;
		byte[] byteArray = null;
		if ((fileArray != null) && (destinationDirectory.isDirectory())) {
			for (int i = 0; i < fileArray.length; i++) {
				file = fileArray[i];
				fileName = file.getName();
				byteArray = readFile(file);
				destinationDirectoryAbsolutePath = destinationDirectory.getAbsolutePath();
				writeFile(destinationDirectoryAbsolutePath + File.separator + fileName, byteArray);
			}
		}
	}

	public boolean deleteDirectory(String directoryName) {
		logger.info("deleteDirectory(" + directoryName + ")");
		return deleteDirectory(new File(directoryName));
	}

	public boolean deleteDirectory(File directory) {
		logger.info("deleteDirectory(" + directory + ")");
		boolean success = false;
		if (directory.isDirectory()) {
			String[] children = directory.list();
			for (int i = 0; i < children.length; i++) {
				success = deleteDirectory(new File(directory, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return directory.delete();
	}

	public File[] getDirectoryFileArray(String directoryName) {
		logger.info("getDirectoryFileArray(" + directoryName + ")");
		return getDirectoryFileArray(new File(directoryName));
	}

	public File[] getDirectoryFileArray(File directory) {
		logger.info("getDirectoryFileArray(" + directory + ")");
		File[] fileArray = null;
		if (directory.isDirectory()) {
			fileArray = directory.listFiles();
		}
		return fileArray;
	}

	public boolean writeFile(String fileName, byte[] byteArray) {
		return writeFile(new File(fileName), byteArray);
	}

	public boolean writeFile(File file, byte[] byteArray) {
		logger.info("writeFile(" + file + ", " + byteArray + ")");
		FileOutputStream fileOutputStream = null;
		boolean success = false;
		newFile(file);
		try {
			fileOutputStream = new FileOutputStream(file);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			bufferedOutputStream.write(byteArray, 0, byteArray.length);
			bufferedOutputStream.flush();
			fileOutputStream.flush();
			bufferedOutputStream.close();
			fileOutputStream.close();
			success = true;
		} catch (FileNotFoundException e) {
			logger.error("writeFile(" + file + ", " + byteArray + ") FileNotFoundException");
			success = false;
		} catch (IOException e) {
			logger.error("writeFile(" + file + ", " + byteArray + ") IOException");
			success = false;
		}
		return success;
	}

	public boolean writeEncryptedFile(File file, byte[] byteArray, char[] password) {
		logger.info("writeEncryptedFile(" + file + ", " + byteArray + ", password)");

		boolean flag = false;
		try {
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecureRandom secureRandom = new SecureRandom();
			byte[] encryptedByteArray = new byte[0];
			byte[] salt = new byte[8];
			secureRandom.nextBytes(salt);
			KeySpec keySpec = new PBEKeySpec(password, salt, 1024, 256);
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(1, secretKeySpec);
			AlgorithmParameters algorithmParameters = cipher.getParameters();
			byte[] initializationVector = ((IvParameterSpec) algorithmParameters
					.getParameterSpec(IvParameterSpec.class)).getIV();
			logger.info(Integer.valueOf(initializationVector.length));
			byte[] cipherText = cipher.doFinal(byteArray);

			encryptedByteArray = appendByteArrays(encryptedByteArray, salt);
			encryptedByteArray = appendByteArrays(encryptedByteArray, initializationVector);
			encryptedByteArray = appendByteArrays(encryptedByteArray, cipherText);
			flag = writeFile(file, encryptedByteArray);
		} catch (NoSuchAlgorithmException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) NoSuchAlgorithmException");
		} catch (InvalidKeySpecException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) InvalidKeySpecException");
		} catch (InvalidKeyException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) InvalidKeyException");
		} catch (NoSuchPaddingException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) NoSuchPaddingException");
		} catch (IllegalBlockSizeException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) IllegalBlockSizeException");
		} catch (BadPaddingException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) BadPaddingException");
		} catch (InvalidParameterSpecException e) {
			logger.error("writeEncryptedFile(" + file + ", " + byteArray + ", password) InvalidParameterSpecException");
		}
		return flag;
	}

	public byte[] readFile(String fileName) {
		return readFile(new File(fileName));
	}

	public byte[] readFile(File file) {
		logger.info("readFile(" + file + ")");
		byte[] byteArray = new byte[0];
		if (file.isFile()) {
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				byteArray = readFile(fileInputStream, (int) file.length());
			} catch (FileNotFoundException e) {
				logger.error("readFile(" + file + ") FileNotFoundException");
			}
		} else {
			logger.error("readFile(" + file + ") (file.isFile() == false)");
		}
		return byteArray;
	}

	public byte[] readFile(InputStream inputStream, int fileLength) {
		logger.info("readFile(" + inputStream + ", " + fileLength + ")");
		byte[] byteArray = new byte[0];
		if ((inputStream != null) && (fileLength > -1)) {
			byteArray = new byte[fileLength];
			try {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				bufferedInputStream.read(byteArray);
			} catch (IOException e) {
				logger.error("readFile(" + inputStream + ", " + fileLength + ") IOException");
			}
		}
		return byteArray;
	}

	public byte[] readFile(InputStream inputStream) {
		logger.info("readFile(" + inputStream + ")");
		byte[] byteArray = new byte[0];
		if (inputStream != null) {
			List<Byte> byteList = new ArrayList();
			byte b = -1;
			try {
				BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
				int integer;
				while ((integer = bufferedInputStream.read()) != -1) {
					byteList.add(Byte.valueOf((byte) integer));
				}
				int byteListSize = byteList.size();
				byteArray = new byte[byteListSize];
				for (int i = 0; i < byteListSize; i++) {
					byteArray[i] = ((Byte) byteList.get(i)).byteValue();
				}
			} catch (IOException e) {
				logger.error("readFile(" + inputStream + ") IOException");
			}
		}
		return byteArray;
	}

	public InputStream readEncryptedFile(File file, char[] password) {
		logger.info("readEncryptedFile(" + file + ", password)");

		InputStream inputStream = null;
		try {
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] byteArray = readFile(file);
			byte[] salt = Arrays.copyOfRange(byteArray, 0, 8);
			byte[] initializationVector = Arrays.copyOfRange(byteArray, 8, 24);
			byte[] cipherText = Arrays.copyOfRange(byteArray, 24, byteArray.length);
			KeySpec keySpec = new PBEKeySpec(password, salt, 1024, 256);
			SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(2, secretKeySpec, new IvParameterSpec(initializationVector));
			String plainText = new String(cipher.doFinal(cipherText), "UTF-8");
			inputStream = new ByteArrayInputStream(plainText.getBytes());
		} catch (NoSuchAlgorithmException e) {
			logger.error("readEncryptedFile(" + file + ", password) NoSuchAlgorithmException");
		} catch (InvalidKeySpecException e) {
			logger.error("readEncryptedFile(" + file + ", password) InvalidKeySpecException");
		} catch (InvalidKeyException e) {
			logger.error("readEncryptedFile(" + file + ", password) InvalidKeyException");
		} catch (NoSuchPaddingException e) {
			logger.error("readEncryptedFile(" + file + ", password) NoSuchPaddingException");
		} catch (IllegalBlockSizeException e) {
			logger.error("readEncryptedFile(" + file + ", password) IllegalBlockSizeException");
		} catch (BadPaddingException e) {
			logger.error("readEncryptedFile(" + file + ", password) BadPaddingException");
		} catch (UnsupportedEncodingException e) {
			logger.error("readEncryptedFile(" + file + ", password) UnsupportedEncodingException");
		} catch (InvalidAlgorithmParameterException e) {
			logger.error("readEncryptedFile(" + file + ", password) InvalidAlgorithmParameterException");
		}
		return inputStream;
	}

	public Properties propertiesLoadFromXML(String fileName) {
		Properties properties = null;
		if (StringUtils.isNotBlank(fileName)) {
			File file = new File(fileName);
			if ((file.exists()) && (!file.isDirectory())) {
				properties = propertiesLoadFromXML(new File(fileName));
			}
		}
		return properties;
	}

	public Properties propertiesLoadFromXML(File fileName) {
		Properties properties = null;
		if (fileName != null) {
			try {
				FileInputStream fileInputStream = new FileInputStream(fileName);
				properties = propertiesLoadFromXML(fileInputStream);
			} catch (FileNotFoundException e) {
				logger.error("propertiesLoadFromXML(" + fileName + ") FileNotFoundException");
				properties = null;
			}
		}
		return properties;
	}

	public Properties propertiesLoadFromXML(InputStream inputStream) {
		logger.debug("propertiesLoadFromXML(" + inputStream + ")");
		Properties properties = null;
		if (inputStream != null) {
			try {
				properties = new Properties();
				properties.loadFromXML(inputStream);
			} catch (InvalidPropertiesFormatException e) {
				logger.error("propertiesLoadFromXML(" + inputStream + ") InvalidPropertiesFormatException");
				properties = null;
			} catch (IOException e) {
				logger.error("propertiesLoadFromXML(" + inputStream + ") IOException");
				properties = null;
			}
		}
		return properties;
	}

	public synchronized boolean propertiesStoreToXML(Properties properties, String fileName, String comment) {
		if ((logger != null) && (logger.isDebugEnabled())) {
			logger.trace("propertiesStoreToXML(" + properties + ", " + fileName + ", " + comment + ")");
		}
		boolean success = false;
		if (newFile(fileName)) {
			Properties sortedProperties = new Properties() {
				private static final long serialVersionUID = 1L;

				public Set<Object> keySet() {
					return Collections.unmodifiableSet(new TreeSet(super.keySet()));
				}
			};
			sortedProperties.putAll(properties);
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(fileName);
				sortedProperties.storeToXML(fileOutputStream, comment);
				fileOutputStream.close();
				success = true;
			} catch (FileNotFoundException e) {
				logger.error("propertiesStoreToXML(" + properties + ", " + fileName + ", " + comment
						+ ") FileNotFoundException");
			} catch (IOException e) {
				logger.error("propertiesStoreToXML(" + properties + ", " + fileName + ", " + comment + ") IOException");
			}
		}
		return success;
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
						
							logger.debug("getProperty(" + key + ", " + defaultValue
									+ ") (this.configurationPropertiesKeySet.contains(" + keyDelta + "))");
						
						property = getConfigurationProperty(keyDelta);
					} else {
					
							logger.debug("getProperty(" + key + ", " + defaultValue
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
//		if (logger.isDebugEnabled()) {
			if (property != null) {
				logger.debug("getProperty(" + key + ", " + defaultValue + ") (" + key + ", " + property + ")");
			} else {
				logger.trace("getProperty(" + key + ", " + defaultValue + ") (" + key + ", " + property + ")");
			}
//		}
		return property;
	}

	protected String getProperty(Properties properties, String key) {
		return properties.getProperty(key);
	}

	protected boolean setConfigurationProperty(String key, String valueBeta) {
		return setConfigurationProperty(this.configurationProperties, this.configurationPropertiesPath, key, valueBeta);
	}

	protected boolean setConfigurationProperty(Properties properties, String propertiesPath, String key, String value) {
		if (logger.isDebugEnabled()) {
			logger.debug("setConfigurationProperty(properties, " + propertiesPath + ", " + key + ", " + value + ")");
		}
		String valueAlpha = properties.getProperty(key);
		boolean flag = false;
		if (value != null) {
			if (valueAlpha != null) {
				if (!value.equals(valueAlpha)) {
					properties.setProperty(key, value);
					flag = propertiesStoreToXML(properties, propertiesPath,
							Utility.formatDate("GMT", "yyyyMMddHHmmss", new Date()));
				}
			} else {
				properties.setProperty(key, value);
				flag = propertiesStoreToXML(properties, propertiesPath,
						Utility.formatDate("GMT", "yyyyMMddHHmmss", new Date()));
			}
		} else if (properties.containsKey(key)) {
			properties.remove(key);
			flag = propertiesStoreToXML(properties, propertiesPath,
					Utility.formatDate("GMT", "yyyyMMddHHmmss", new Date()));
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

	protected static InputStream getProcessInputStream(Process process) {
		InputStream inputStream = null;
		inputStream = process.getInputStream();
		return inputStream;
	}

	protected static OutputStream getProcessOutputStream(Process process) {
		OutputStream outputStream = null;
		outputStream = process.getOutputStream();
		return outputStream;
	}

	protected static InputStream getProcessErrorStream(Process process) {
		InputStream inputStream = null;
		inputStream = process.getErrorStream();
		return inputStream;
	}

	protected Process getProcess(String command) {
		return newProcess(command);
	}

	protected Process newProcess(String command) {
		if (logger.isDebugEnabled()) {
			logger.trace("newProcess(" + command + ")");
		}
		Process process = null;
		if (StringUtils.isNotBlank(command)) {
			try {
				process = this.runtime.exec(command);
			} catch (IOException e) {
				logger.error("newProcess(" + command + ") IOException");
				process = null;
			}
		}
		return process;
	}

	protected int processWaitFor(Process process) {
		int exitValue = -1;
		try {
			exitValue = process.waitFor();
			if (logger.isDebugEnabled()) {
				logger.trace("processWaitFor(" + process + ") (exitValue = " + exitValue + ")");
			}
		} catch (InterruptedException e) {
			logger.error("processWaitFor(" + process + ") InterruptedException");
			exitValue = -1;
		}
		return exitValue;
	}

	protected void inputStreamClose(InputStream inputStream) {
		if (logger.isDebugEnabled()) {
			logger.trace(this + ".inputStreamClose(" + inputStream + ")");
		}
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.warn(this + ".inputStreamClose(" + inputStream + ") IOException");
			}
		} else {
			logger.warn(this + ".inputStreamClose(" + inputStream + ") (inputStream = " + inputStream + ")");
		}
	}

	protected void outputStreamClose(OutputStream outputStream) {
		if (logger.isDebugEnabled()) {
			logger.trace(this + ".outputStreamClose(" + outputStream + ")");
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.warn(this + ".outputStreamClose(" + outputStream + ") IOException");
			}
		} else {
			logger.warn(this + ".outputStreamClose(" + outputStream + ") (outputStream = null)");
		}
	}

	protected int execute(String command) {
	
			logger.info("execute(" + command + ")");
		
		int processWaitFor = -1;
		this.process = getProcess(command);
		if (this.process != null) {
			BufferedReader bufferedReaderInputStream = new BufferedReader(
					new InputStreamReader(getProcessInputStream(this.process)));
			BufferedReader bufferedReaderErrorStream = new BufferedReader(
					new InputStreamReader(getProcessErrorStream(this.process)));
			try {
				String line;
				while (!StringUtils.isBlank(line = bufferedReaderInputStream.readLine())) {

					if (logger.isDebugEnabled()) {
						logger.trace("execute(" + command + ") (line = " + line + ")");
					}
				}
				while (!StringUtils.isBlank(line = bufferedReaderErrorStream.readLine())) {
					logger.error("execute(" + command + ") (line = " + line + ")");
				}
			} catch (IOException e) {
				logger.error("execute(" + command + ") IOException");
			}
			processWaitFor = processWaitFor(this.process);
			if (processWaitFor > 0) {
				logger.warn("execute(" + command + ") (processWaitFor = " + processWaitFor + ")");
			} else if (logger.isDebugEnabled()) {
				logger.trace("execute(" + command + ") (processWaitFor = " + processWaitFor + ")");
			}
		}
		return processWaitFor;
	}

	private void deleteLinuxLockFile(String device) {
		String fileName = "/var/lock/LCK.." + device;
		File file = new File(fileName);
		file.setWritable(true);
		file.setExecutable(true);
		file.setReadable(true);
		if (file.exists()) {
			if (logger.isDebugEnabled()) {
				logger.debug("deleteLinuxLockFile(" + device + ") (lockFile.exists())");
			}
			file.delete();
		} else if (logger.isDebugEnabled()) {
			logger.info("deleteLinuxLockFile(" + device + ") (!this.lockFile.exists())");
		}
	}

	private byte[] appendByteArrays(byte[] byteArray, byte[] postByteArray) {
		byte[] one = byteArray;
		byte[] two = postByteArray;
		byte[] combined = new byte[one.length + two.length];
		for (int i = 0; i < combined.length; i++) {
			combined[i] = (i < one.length ? one[i] : two[(i - one.length)]);
		}
		return combined;
	}
}

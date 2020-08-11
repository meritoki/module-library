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

package com.meritoki.module.library.model.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.meritoki.module.library.model.Module;
import com.meritoki.module.library.model.Utility;

public class Protocol {
	protected Logger logger = Logger.getLogger(Protocol.class.getName());
	@JsonProperty
	protected ProtocolType type;
	@JsonProperty
	protected ProtocolState state = ProtocolState.START;
	@JsonIgnore
	protected byte[] byteArray = new byte[0];
	@JsonIgnore
	protected int[] startIntArray = new int[2];
	@JsonIgnore
	protected int[] lengthIntArray = new int[2];
	@JsonIgnore
	protected int[] versionIntArray = new int[3];
	@JsonIgnore
	protected int[] typeIntArray = new int[1];
	@JsonIgnore
	protected int[] offsetIntArray = new int[4];
	@JsonIgnore
	protected int[] acknowledgeIntArray = new int[4];
	@JsonIgnore
	protected int[] dataIntArray = new int[0];
	@JsonIgnore
	protected int[] crcIntArray = new int[2];
	@JsonIgnore
	protected int start = 18502;
	@JsonIgnore
	protected int length;
	@JsonProperty
	protected int version = 2;
	@JsonProperty
	protected int offset = 0;
	@JsonProperty
	protected int acknowledge = 0;
	@JsonProperty
	public String data;
	protected int crc;
	@JsonProperty
	protected int tryCount = 0;
	@JsonProperty
	protected double timeout = 0.5D;
	@JsonIgnore
	protected int index = 0;

	@JsonIgnore
	public Object serialize(ProtocolType type, int offset, int acknowledge, String data) {
		int[] intArray = new int[0];
		this.type = type;
		this.offset = offset;
		this.acknowledge = acknowledge;
		this.byteArray = new byte[0];
		if ((type == ProtocolType.MESSAGE) && (data != null)) {
			this.data = data;
			this.length += this.data.length();
			this.dataIntArray = Utility.byteArrayToIntArray(data.getBytes());
		}
		this.length += 2;
		this.length += 2;
		this.length += 3;
		this.length += 1;
		this.length += 4;
		this.length += 4;
		this.length += 2;
		this.startIntArray = Utility.intForwardMaskShifter(this.start, 255, 8, 2);
		this.lengthIntArray = Utility.intForwardMaskShifter(this.length, 255, 8, 2);
		this.versionIntArray = Utility.intForwardMaskShifter(this.version, 255, 8, 3);
		this.typeIntArray = Utility.intForwardMaskShifter(this.getType(this.type), 255, 8, 1);
		this.offsetIntArray = Utility.intForwardMaskShifter(this.offset, 255, 8, 4);
		this.acknowledgeIntArray = Utility.intForwardMaskShifter(this.acknowledge, 255, 8, 4);
		intArray = Utility.appendIntArrays(intArray, this.startIntArray);
		intArray = Utility.appendIntArrays(intArray, this.lengthIntArray);
		intArray = Utility.appendIntArrays(intArray, this.versionIntArray);
		intArray = Utility.appendIntArrays(intArray, this.typeIntArray);
		intArray = Utility.appendIntArrays(intArray, this.offsetIntArray);
		intArray = Utility.appendIntArrays(intArray, this.acknowledgeIntArray);
		if (type == ProtocolType.MESSAGE) {
			intArray = Utility.appendIntArrays(intArray, this.dataIntArray);
		}
		this.crcIntArray = Utility.intForwardMaskShifter(computeCRC16One(intArray), 255, 8, 2);
		intArray = Utility.appendIntArrays(intArray, this.crcIntArray);
		this.byteArray = Utility.intArrayToByteArray(intArray);
		return this.byteArray;
	}


	@JsonIgnore
	public byte[] deserialize(byte[] byteArray) {
		int[] intArray = new int[0];
		int byteArrayLength = byteArray.length;
		int dataLength = 0;
		int index = -1;
		boolean flag = true;
		while (index < byteArrayLength && flag) {
			int integer, i;
			switch (this.state) {
			case START:
				this.startIntArray = Utility.intForwardMaskShifter(this.start, 255, 8, 2);
				index++;
				if (index + 2 < byteArrayLength) {
					for (int j = 0; j < 2; j++) {
						if (this.startIntArray[j] == Utility.byteToInteger(byteArray[index + j]) && j == 2 - 1) {
							this.state = ProtocolState.LENGTH;
							intArray = Utility.appendIntArrays(intArray, this.startIntArray);
							index += 2;
						}
					}
					continue;
				}
				this.state = ProtocolState.BAD;
				index += 2;
				flag = false;
				continue;
			case LENGTH:
				for (i = 0; i < 2; i++) {
					if (index + i < byteArrayLength)
						this.lengthIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
				}
				this.length = Utility.intReverseMaskShifter(this.lengthIntArray, 8);
				intArray = Utility.appendIntArrays(intArray, this.lengthIntArray);
				dataLength = this.length - 18;
				this.dataIntArray = new int[dataLength];
				index += 2;
				this.state = ProtocolState.VERSION;
				continue;
			case VERSION:
				this.versionIntArray = new int[3];
				for (i = 0; i < 3; i++) {
					if (index + i < byteArrayLength)
						this.versionIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
				}
				intArray = Utility.appendIntArrays(intArray, this.versionIntArray);
				this.version = Utility.intReverseMaskShifter(this.versionIntArray, 8);
				index += 3;
				this.state = ProtocolState.TYPE;
				continue;
			case TYPE:
				this.typeIntArray = new int[1];
				for (i = 0; i < 1; i++) {
					if (index + i < byteArrayLength)
						this.typeIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
				}
				intArray = Utility.appendIntArrays(intArray, this.typeIntArray);
				this.type = this.getProtocolType(Utility.intReverseMaskShifter(this.typeIntArray, 8));
				index++;
				this.state = ProtocolState.OFFSET;
				continue;
			case OFFSET:
				this.offsetIntArray = new int[4];
				for (i = 0; i < 4; i++) {
					if (index + i < byteArrayLength)
						this.offsetIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
				}
				intArray = Utility.appendIntArrays(intArray, this.offsetIntArray);
				this.offset = Utility.intReverseMaskShifter(this.offsetIntArray, 8);
				index += 4;
				this.state = ProtocolState.ACKNOWLEDGE;
				continue;
			case ACKNOWLEDGE:
				this.acknowledgeIntArray = new int[4];
				for (i = 0; i < 4; i++) {
					if (index + i < byteArrayLength)
						this.acknowledgeIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
				}
				intArray = Utility.appendIntArrays(intArray, this.acknowledgeIntArray);
				this.acknowledge = Utility.intReverseMaskShifter(this.acknowledgeIntArray, 8);
				index += 4;
				this.state = ProtocolState.DATA;
				continue;
			case DATA:
				this.dataIntArray = new int[dataLength];
				this.data = "";
				for (i = 0; i < dataLength; i++) {
					if (index + i < byteArrayLength)
						this.dataIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
					this.data += (char) this.dataIntArray[i];
				}
				intArray = Utility.appendIntArrays(intArray, this.dataIntArray);
				index += dataLength;
				this.state = ProtocolState.CRC;
				continue;
			case CRC:
				this.crcIntArray = new int[2];
				for (i = 0; i < 2; i++) {
					if (index + i < byteArrayLength)
						this.crcIntArray[i] = Utility.byteToInteger(byteArray[index + i]);
				}
				index += 2;
				integer = Utility.intReverseMaskShifter(this.crcIntArray, 8);
				this.crc = computeCRC16One(intArray);
				if (this.crc == integer) {
					this.state = ProtocolState.GOOD;
				} else {
					this.state = ProtocolState.BAD;
				}
				flag = false;
				continue;
			}
			flag = false;
			this.state = ProtocolState.BAD;
		}
		if (index <= byteArrayLength) {
			byteArray = Arrays.copyOfRange(byteArray, index, byteArrayLength);
		} else {
			byteArray = new byte[0];
		}
		return byteArray;
	}


	@JsonIgnore
	public Object serialize(Object object) {
		if ((object instanceof byte[])) {
			setByteArray((byte[]) object);
		} else if ((object instanceof String)) {
			setByteArray(((String) object).getBytes());
		}
		return object;
	}

	@JsonIgnore
	public ProtocolType getType() {
		return this.type;
	}
	@JsonIgnore
	public int getMessageOffset() {
		return this.offset;
	}
	@JsonIgnore
	public int getMessageAcknowledged() {
		return this.acknowledge;
	}
	@JsonIgnore
	public String getData() {
		return this.data;
	}
	@JsonIgnore
	public int getTryCount() {
		return this.tryCount;
	}

//	public int getDataLength() {
//		return this.dataLength;
//	}
	@JsonIgnore
	public ProtocolState getState() {
		return this.state;
	}
	@JsonIgnore
	public double getTimeout() {
		return this.timeout;
	}
	@JsonIgnore
	public byte[] getByteArray() {
		return this.byteArray;
	}
	@JsonIgnore
	public int getIndex() {

		logger.finest("getIndex() (this.index = " + this.index + ")");

		return this.index;
	}
//
//	public byte[] getDataByteArray() {
//		return this.dataByteArray;
//	}

//	public void setObject(Object object) {
//		this.object = object;
//	}
	@JsonIgnore
	public void setTryCount(int tryCount) {
		this.tryCount = tryCount;
	}
	@JsonIgnore
	public void setMessageOffset(int messageOffset) {
		this.offset = messageOffset;
	}
	@JsonIgnore
	public void setMessageAcknowledged(int messageAcknowledge) {
		this.acknowledge = messageAcknowledge;
	}
	@JsonIgnore
	public void setIndex(int index) {
		this.index = index;
	}
	@JsonIgnore
	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;

		logger.finest(this + ".setByteArray(" + byteArray + ") (byteArray = "
				+ Utility.byteArrayToByteArrayString(this.byteArray) + ")");

	}
	@JsonIgnore
	public int computeCRC8(int[] intArray) {
		int crc = 0;

		int[] crcIntArray = { 0, 94, 188, 226, 97, 63, 221, 131, 194, 156, 126, 32, 163, 253, 31, 65, 157, 195, 33, 127,
				252, 162, 64, 30, 95, 1, 227, 189, 62, 96, 130, 220, 35, 125, 159, 193, 66, 28, 254, 160, 225, 191, 93,
				3, 128, 222, 60, 98, 190, 224, 2, 92, 223, 129, 99, 61, 124, 34, 192, 158, 29, 67, 161, 255, 70, 24,
				250, 164, 39, 121, 155, 197, 132, 218, 56, 102, 229, 187, 89, 7, 219, 133, 103, 57, 186, 228, 6, 88, 25,
				71, 165, 251, 120, 38, 196, 154, 101, 59, 217, 135, 4, 90, 184, 230, 167, 249, 27, 69, 198, 152, 122,
				36, 248, 166, 68, 26, 153, 199, 37, 123, 58, 100, 134, 216, 91, 5, 231, 185, 140, 210, 48, 110, 237,
				179, 81, 15, 78, 16, 242, 172, 47, 113, 147, 205, 17, 79, 173, 243, 112, 46, 204, 146, 211, 141, 111,
				49, 178, 236, 14, 80, 175, 241, 19, 77, 206, 144, 114, 44, 109, 51, 209, 143, 12, 82, 176, 238, 50, 108,
				142, 208, 83, 13, 239, 177, 240, 174, 76, 18, 145, 207, 45, 115, 202, 148, 118, 40, 171, 245, 23, 73, 8,
				86, 180, 234, 105, 55, 213, 139, 87, 9, 235, 181, 54, 104, 138, 212, 149, 203, 41, 119, 244, 170, 72,
				22, 233, 183, 85, 11, 136, 214, 52, 106, 43, 117, 151, 201, 74, 20, 246, 168, 116, 42, 200, 150, 21, 75,
				169, 247, 182, 232, 10, 84, 215, 137, 107, 53 };
		for (int i = 0; i < intArray.length; i++) {
			crc = crcIntArray[(crc ^ intArray[i] & 0xFF)];
		}
		return crc;
	}
	@JsonIgnore
	public int computeCRC8Two(int[] intArray) {
		int crc = 0;
		for (int i = 0; i < intArray.length; i++) {
			int byteInt = intArray[i];
			int bit;
			for (int j = 8; j > 0; j--) {
				bit = (crc ^ byteInt) & 0x1;
				crc >>= 1;
				if (bit > 0) {
					crc ^= 0x8C;
				}
				byteInt >>= 1;
			}
		}
		String intArrayString = "";
		int[] arrayOfInt = intArray;
		int bit = intArray.length;
		for (int j = 0; j < bit; j++) {
			int i = arrayOfInt[j];
			intArrayString = intArrayString + "0x"
					+ Utility.getZeroFilledRightJustifiedString(Integer.toHexString(i & 0xFF), 2) + ",";
		}

		logger.fine("computeCRC8Two(" + intArray + ") (intArray = " + intArrayString + ") (crc = " + "0x"
				+ Utility.getZeroFilledRightJustifiedString(Integer.toHexString(crc), 2) + " = "
				+ Utility.getZeroFilledRightJustifiedString(Integer.toBinaryString(crc), 8) + "))");

		return crc;
	}
	@JsonIgnore
	public static int computeCRC16Two(int[] intArray) {
		int crc = 65535;
		int intArrayLength = intArray.length;
		for (int i = 0; i < intArrayLength; i++) {
			int integer = intArray[i];
			for (int j = 0; j < 8; j++) {
				int feedback;

				if (((integer ^ crc) & 0x1) == 1) {
					feedback = 40961;
				} else {
					feedback = 0;
				}
				integer >>= 1;
				crc >>= 1;
				crc ^= feedback;
			}
		}
		return crc;
	}
	@JsonIgnore
	public static int computeCRC16One(int[] intArray) {
		int crc = 65535;
		int intArrayLength = intArray.length;
		for (int i = 0; i < intArrayLength; i++) {
			int integer = intArray[i];
			integer &= 0xFF;
			integer = (integer ^ crc) & 0xFF;
			integer = (integer ^ integer << 4) & 0xFF;
			crc = crc >> 8 ^ integer << 8 ^ integer << 3 ^ integer >> 4;
		}
		return crc;
	}
	@JsonIgnore
	private int getType(ProtocolType protocolType) {
		int type = 0;
		switch (protocolType) {
		case UNUSED: {
			type = 1;
			break;
		}
		case ADVERTISEMENT: {
			type = 2;
			break;
		}
		case MESSAGE: {
			type = 3;
			break;
		}
		case CONNECT: {
			type = 4;
			break;
		}
		case DISCONNECT: {
			type = 5;
			break;
		}
		case RESEND: {
			type = 6;
			break;
		}
		}
		return type;
	}
	@JsonIgnore
	private ProtocolType getProtocolType(int type) {
		ProtocolType protocolType = ProtocolType.UNUSED;
		switch (type) {
		case 1: {
			protocolType = ProtocolType.UNUSED;
			break;
		}
		case 2: {
			protocolType = ProtocolType.ADVERTISEMENT;
			break;
		}
		case 3: {
			protocolType = ProtocolType.MESSAGE;
			break;
		}
		case 4: {
			protocolType = ProtocolType.CONNECT;
			break;
		}
		case 5: {
			protocolType = ProtocolType.DISCONNECT;
			break;
		}
		case 6: {
			protocolType = ProtocolType.RESEND;
			break;
		}
		}
		return protocolType;
	}

	@Override
	public String toString() {
		String string = "";
		ObjectWriter ow = new ObjectMapper().writer();// .withDefaultPrettyPrinter();
		try {
			string = ow.writeValueAsString(this);
		} catch (IOException ex) {
			logger.severe("IOException " + ex.getMessage());
		}
		return string;
	}
}

//public List<String> parse(int[] intArray) {
//ArrayList<String> stringList = new ArrayList();
//int index = 0;
//
//String string = "";
//while (index < intArray.length) {
//	int integer = intArray[index];
//	if ((32 <= integer) && (integer <= 126)) {
//		string = string + (char) integer;
//	} else if (integer == 0) {
//		if (StringUtils.isNotBlank(string)) {
//			stringList.add(string);
//		}
//		string = "";
//	}
//	index++;
//}
//return stringList;
//}

//	public byte getAddress() {
//		return this.address;
//	}
//
//	public void setAddress(byte address) {
//		this.address = address;
//	}
// public static final int START = 0;
//public static final int LENGTH = 1;
//public static final int VERSION = 2;
//public static final int TYPE = 3;
//public static final int OFFSET = 4;
//public static final int ACKNOWLEDGE = 5;
//public static final int DATA = 6;
//public static final int CRC = 7;
//public static final int GOOD = 8;
//public static final int BAD = 9;
//
//public static final int UNUSED = 1;
//public static final int ADVERTISEMENT = 2;
//public static final int MESSAGE = 3;
//public static final int CONNECT = 4;
//public static final int DISCONNECT = 5;
//public static final int RESEND_REQUEST = 6;

//protected final int startLength = 2;
//protected final int lengthLength = 2;
//protected final int versionLength = 3;
//protected final int typeLength = 1;
//protected final int offsetLength = 4;
//protected final int acknowledgeLength = 4;
//

//protected int dataLength = 0;
//protected final int crcLength = 2;

// protected byte address;
//protected boolean checkSum = false;

// public Object deserialize(Object object)
// {
// if ((object instanceof byte[]))
// {
// object = new String(this.byteArray);
// this.state = 8;
// }
// return object;
// }


//public Object deserialize(Object object)
//{
//if ((object instanceof byte[]))
//{
//  object = new String(this.byteArray);
//  this.state = 8;
//}
//return object;
//}

//protected int byteCount;
//protected int byteArrayLength;

//protected byte[] dataByteArray = new byte[0];

//public byte getAddress() {
//return this.address;
//}
//
//public void setAddress(byte address) {
//this.address = address;
//}

//public void setDataByteArray(byte[] dataByteArray) {
//this.dataByteArray = dataByteArray;
//
//logger.finest(this + ".setDataByteArray(" + dataByteArray + ") (dataByteArray = "
//		+ Utility.byteArrayToByteArrayString(this.dataByteArray) + ")");
//
//}

//public int setByteCount(int byteCount) {
//
//logger.finest("setByteCount(" + byteCount + ")");
//
//return this.byteCount = byteCount;
//}

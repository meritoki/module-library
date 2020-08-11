package com.meritoki.module.library.model.data;
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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Data {
	protected Logger logger = Logger.getLogger(Data.class.getName());
	@JsonProperty
	protected int destinationID;
	@JsonProperty
	protected int sourceID;
	@JsonProperty
	protected DataType type;
	@JsonProperty
	private double delay = 0.0D;
	@JsonProperty
	protected Object object;
	@JsonIgnore
	private List<Object> outputObjectList;
	@JsonProperty
	private double start = 0.0D;
	@JsonProperty
	private double stop = 0.0D;

	public Data(int destinationID, int sourceID, DataType type, double delay, Object object,
			List<Object> outputObjectList) {
		this.destinationID = destinationID;
		this.sourceID = sourceID;
		this.type = type;
		this.delay = delay;
		if (this.delay > 0.0D) {
			this.start = newStart();
			this.stop = (this.start + this.delay);
		}
		this.object = object;
		this.outputObjectList = outputObjectList;
	}

	@JsonIgnore
	public int getDestinationID() {
		return this.destinationID;
	}

	@JsonIgnore
	public int getSourceID() {
		return this.sourceID;
	}

	@JsonIgnore
	public DataType getType() {
		return this.type;
	}

	@JsonIgnore
	private double newStart() {
		Date creationDate = new Date(System.currentTimeMillis());
		double creationDateDouble = creationDate.getTime();
		return creationDateDouble / 1000.0D;
	}

	@JsonIgnore
	public double getStop() {
		return this.stop;
	}

	@JsonIgnore
	public Object getObject() {
		return this.object;
	}

	@JsonIgnore
	public boolean objectListAdd(Data container) {
		boolean flag = false;
		if (this.outputObjectList != null) {
			synchronized (this.outputObjectList) {
				this.outputObjectList.add(container);
				this.outputObjectList.notify();
				flag = true;
			}
		}
		return flag;
	}

	@JsonIgnore
	public List<Object> getOutputObjectList() {
		return this.outputObjectList;
	}

	@Override
	public String toString() {
		String string = "";
		ObjectWriter ow = new ObjectMapper().writer();// .withDefaultPrettyPrinter();
		try {
			string = ow.writeValueAsString(this);
		} catch (IOException ex) {
			logger.warning("IOException " + ex.getMessage());
		}
		return string;
	}
}

//public static final int INPUT = 2;
//public static final int OUTPUT = 1;
//public static final int POLL = 10;
//public static final int ACKNOWLEDGE = 1233;
//public static final int DELAY = 99948823;
//public static final int BLOCK = 13;
//public static final int UNBLOCK = 14;

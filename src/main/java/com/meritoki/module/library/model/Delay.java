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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Delay extends Node {
	private List<Object> objectList;
	private double delayMax = 999.0D;
	private double delay;

	public Delay(int id, Module module) {
		super(Integer.valueOf(id), module);
	}

	public void initialize() {
		super.initialize();
		this.objectList = new ArrayList();
		this.delay = this.delayMax;
		this.delayMax = Utility.stringToDouble(this.idProperties.getProperty("delayMax"));
	}

	protected double send() {
		Date nowDate = new Date(System.currentTimeMillis());
		double nowDateDouble = nowDate.getTime();
		double now = nowDateDouble / 1000.0D;
		int index = 0;
		Object object;
		do {
			if (((object = objectListGet(index)) instanceof Data)) {
				Data container = (Data) object;
				if (0.0D >= container.getExpirationTime() - now) {
					objectListRemove(index);
					container.objectListAdd(container);
				}
			}
			index++;
		} while (object != null);
		return getDelayMin();
	}

	protected double getDelayMin() {
		double delayMin = this.delayMax;
		Date nowDate = new Date(System.currentTimeMillis());
		double nowDateDouble = nowDate.getTime();
		double now = nowDateDouble / 1000.0D;
		int pendingObjectListIndex = 0;
		Object object;
		do {
			if (((object = objectListGet(pendingObjectListIndex)) instanceof Data)) {
				Data container = (Data) object;
				if (container.getExpirationTime() - now < delayMin) {
					delayMin = container.getExpirationTime() - now;
				}
			}
			pendingObjectListIndex++;
		} while (object != null);
		return delayMin;
	}

	protected Object objectListGet(int index) {
		Object object = null;
		if (index < this.objectList.size()) {
			object = this.objectList.get(index);
		}
		return object;
	}

	protected Object objectListRemove(int index) {
		Object object = null;
		if (index < this.objectList.size()) {
			object = this.objectList.remove(index);
		}
		return object;
	}

	protected void objectListAdd(Object object) {
		this.objectList.add(object);
	}

	protected void inputState(Object object) {
		while ((0.0D >= this.delay) && (this.run)) {
			this.delay = send();
		}
		if ((object instanceof Data)) {
			Data container;
			if ((container = (Data) object) != null) {
				objectListAdd(container);
			}
		}
		this.delay = getDelayMin();
	}

	protected void inputData(Object object) {
		add(new Data(this.id.intValue(), this.id.intValue(), 2, 0.0D, object, null));
	}
}

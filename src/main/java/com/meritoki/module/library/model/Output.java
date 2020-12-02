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
import java.io.OutputStream;

import com.meritoki.module.library.model.data.Data;
import com.meritoki.module.library.model.protocol.Protocol;

public class Output extends Node {
	protected OutputStream outputStream = null;

	public Output(int id, Module module, OutputStream outputStream) {
		super(Integer.valueOf(id), module);
		this.outputStream = outputStream;
	}

	@Override
	public void initialize() {
		super.initialize();
		this.setState(State.INPUT);
	}

	@Override
	public void destroy() {
		if (!this.destroy) {
			super.destroy();
			outputStreamClose(this.outputStream);
		}
	}

	@Override
	protected void inputState(Object object) {
		if ((object instanceof Data)) {
			Data data = (Data) object;
			object = data.getObject();
			switch (data.getType()) {
			case OUTPUT:{
				output(object);
				break;
			}
			default: {
				logger.warning("inputState("+object+") !DataType.OUTPUT");
			}
			}
		}
	}

	public void output(Object object) {
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			byte[] byteArray = protocol.getByteArray();
			try {
				this.outputStream.write(byteArray);
				this.outputStream.flush();
				logger.fine("output("+object+") written");
			} catch (IOException e) {
				logger.severe("output(" + object + ") IOException");
				this.destroy();
			}
		}
	}

	protected void outputStreamClose(OutputStream outputStream) {

		logger.finest(this + ".outputStreamClose(" + outputStream + ")");
		
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.warning(this + ".outputStreamClose(" + outputStream + ") IOException");
			}
		} else {
			logger.warning(this + ".outputStreamClose(" + outputStream + ") (outputStream = null)");
		}
	}
}

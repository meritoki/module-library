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
import java.io.OutputStream;

public class Output extends Node {
	protected OutputStream outputStream = null;

	public Output(int id, Module module, OutputStream outputStream) {
		super(Integer.valueOf(id), module);
		logger.debug("Output("+id+", "+module+", "+outputStream+")");
		this.outputStream = outputStream;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void destroy() {
		if (!this.destroy) {
			super.destroy();
			outputStreamClose(this.outputStream);
		}
	}

	public void output(Object object) {
		logger.info("output("+object+")");
		if ((object instanceof Protocol)) {
			Protocol protocol = (Protocol) object;
			byte[] byteArray = protocol.getByteArray();
			try {
				this.outputStream.write(byteArray);
				this.outputStream.flush();
				logger.info("output("+object+") written");
			} catch (IOException e) {
				logger.error("output(" + object + ") IOException");
				setState(0);
			}
		}
	}

	@Override
	protected void inputState(Object object) {
		if ((object instanceof Data)) {
			Data container = (Data) object;
			object = container.getObject();
			switch (container.getType()) {
			case 1:
				output(object);
			}
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
}

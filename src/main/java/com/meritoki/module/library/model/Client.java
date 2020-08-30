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

import com.meritoki.module.library.model.data.Data;
import com.meritoki.module.library.model.data.DataType;
import com.meritoki.module.library.model.protocol.Protocol;
import com.meritoki.module.library.model.protocol.ProtocolType;

public class Client extends Web {
	
	public static void main(String[] args) {
		Client client = new Client(0);
		CountDownLatch countDownLatch;
		client.setCountDownLatch(countDownLatch = new CountDownLatch(1));
		client.start();
		while(client.getState() == State.CONNECTION) {
//			System.out.println("Waiting...");
		}		
		System.out.println("SEND");
		client.add(new Data(0,0,DataType.OUTPUT,0,"{Hello World}",null));
	}
	
	public Client(int id) {
		super(id);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		this.setState(State.CONNECTION);
	}
	
    @Override
    protected void output(Object object) {
        if(object instanceof String) {
            String string = (String) object;
            Protocol protocol = new Protocol();
            protocol.serialize(ProtocolType.MESSAGE,this.protocol.getMessageOffset(),this.protocol.getMessageAcknowledged(),string);
            super.output(protocol);
        }
    }
}

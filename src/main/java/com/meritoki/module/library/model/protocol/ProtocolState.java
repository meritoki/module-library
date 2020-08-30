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
package com.meritoki.module.library.model.protocol;

public enum ProtocolState {
	START,//0
	LENGTH,//1
	VERSION,//2
	TYPE,//3
	OFFSET,//4
	ACKNOWLEDGE,//5
	DATA,//6
	CRC,//7
	GOOD,//8
	BAD//9
}

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

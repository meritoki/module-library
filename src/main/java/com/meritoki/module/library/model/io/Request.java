package com.meritoki.module.library.model.io;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Request {
	@JsonProperty
	public String uuid;
	@JsonProperty
	public Method method;
	@JsonProperty
	public String uri;
	@JsonProperty
	public String key;//key can do a lot of things when sent to server, such as tell server how to Parse the value
	@JsonProperty
	public String value;//value can hold a JSON object;
	
	
    public Request() {
        this.uuid = UUID.randomUUID().toString();
    }

    @JsonIgnore
    @Override
    public String toString() {
        String string = "";
        ObjectWriter ow = new ObjectMapper().writer();//.withDefaultPrettyPrinter();
        try {
            string = ow.writeValueAsString(this);
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return string;
    }
}

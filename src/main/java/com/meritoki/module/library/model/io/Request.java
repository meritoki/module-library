package com.meritoki.module.library.model.io;

import java.io.IOException;
import java.util.Map;
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
	public Map<String,String> map;
	
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

package com.meritoki.module.library.model.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Response {
	
	@JsonProperty
	public String uuid;
	@JsonProperty
	public Map<String,String> map = new HashMap<>();
	
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

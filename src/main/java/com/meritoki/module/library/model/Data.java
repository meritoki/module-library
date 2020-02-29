package com.meritoki.module.library.model;

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


import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Data
{
  protected static Logger logger = LogManager.getLogger(Data.class);
  public static final int INPUT = 2;
  public static final int OUTPUT = 1;
  public static final int POLL = 10;
  public static final int ACKNOWLEDGE = 1233;
  public static final int DELAY = 99948823;
  public static final int BLOCK = 13;
  public static final int UNBLOCK = 14;
  protected int destinationID;
  protected int sourceId;
  protected int type;
  private double delayTime = 0.0D;
  protected Object object;
  private List<Object> outputObjectList;
  private double creationTime = 0.0D;
  private double expirationTime = 0.0D;
  
  public Data(int destinationId, int sourceId, int type, double delayTime, Object object, List<Object> outputObjectList)
  {
    this.destinationID = destinationId;
    this.sourceId = sourceId;
    this.type = type;
    this.delayTime = delayTime;
    if (this.delayTime > 0.0D)
    {
      this.creationTime = newCreationTime();
      this.expirationTime = (this.creationTime + this.delayTime);
    }
    this.object = object;
    this.outputObjectList = outputObjectList;
  }
  
  public int getDestinationID()
  {
    return this.destinationID;
  }
  
  public int getSourceID()
  {
    return this.sourceId;
  }
  
  public int getType()
  {
    return this.type;
  }
  
  private double newCreationTime()
  {
    Date creationDate = new Date(System.currentTimeMillis());
    double creationDateDouble = creationDate.getTime();
    return creationDateDouble / 1000.0D;
  }
  
  public double getExpirationTime()
  {
    return this.expirationTime;
  }
  
  public Object getObject()
  {
    return this.object;
  }
  
  public boolean outputObjectListAdd(Data container)
  {
    boolean flag = false;
    if (this.outputObjectList != null) {
      synchronized (this.outputObjectList)
      {
        this.outputObjectList.add(container);
        this.outputObjectList.notify();
        flag = true;
      }
    }
    return flag;
  }
  
  public String toString()
  {
    String string = super.toString();
    String stringPackage = getClass().getPackage().getName();
    if (stringPackage != null) {
      string = string.replaceFirst("^" + stringPackage + ".", "");
    }
    return string;
  }
  
  public List<Object> getOutputObjectList()
  {
    return this.outputObjectList;
  }
}

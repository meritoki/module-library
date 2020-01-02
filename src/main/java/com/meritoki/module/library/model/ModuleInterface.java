package com.meritoki.module.library.model;

public abstract interface ModuleInterface
  extends Runnable
{
  public abstract void start();
  
  public abstract void initialize();
  
  public abstract void run();
  
  public abstract void stop();
  
  public abstract void destroy();
  
  public abstract boolean getStart();
  
  public abstract boolean getRun();
  
  public abstract boolean getDestroy();
  
  public abstract boolean getProtect();
  
  public abstract int getID();
  
  public abstract void add(Object paramObject);
  
  public abstract Object remove(int paramInt);
  
  public abstract void rootAdd(Object paramObject);
  
  public abstract Object load(Integer paramInteger, Object paramObject);
}

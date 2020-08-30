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

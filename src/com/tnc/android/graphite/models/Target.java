/*
 * Copyright 2012 Tomas Joelsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tnc.android.graphite.models;


public class Target extends SimpleObservable<Target>
{
  private int id=-1;
  private String hash="";
  private String name="";
  private boolean enabled=false;
  private boolean expandable=false;

  //Not persistent variables
  private boolean placeholder=false;
  private boolean loaded=true;

  public int getId()
  {
    return id;
  }
  public void setId(int id)
  {
    this.id=id;
    notifyObservers(this);
  }

  public String getHash()
  {
    return hash;
  }
  public void setHash(String hash)
  {
    this.hash=hash;
    notifyObservers(this);
  }

  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name=name;
    notifyObservers(this);
  }

  public boolean isEnabled()
  {
    return enabled;
  }
  public void setEnabled(boolean enabled)
  {
    this.enabled=enabled;
    notifyObservers(this);
  }

  public boolean isExpandable()
  {
    return expandable;
  }
  public void setExpandable(boolean expandable)
  {
    this.expandable=expandable;
    notifyObservers(this);
  }

  // Not a persistent variable
  public boolean isPlaceholder()
  {
    return placeholder;
  }
  public void setPlaceholder(boolean placeholder)
  {
    this.placeholder=placeholder;
  }

  //Not a persistent variable
  public boolean isLoaded()
  {
    return loaded;
  }
  public void setLoaded(boolean loaded)
  {
    this.loaded=loaded;
  }

  @Override
  synchronized public Target clone()
  {
    Target t=new Target();
    t.setId(id);
    t.setName(name);
    t.setEnabled(enabled);
    t.setExpandable(expandable);
    return t;
  }

  synchronized public void consume(Target t)
  {
    this.id=t.getId();
    this.name=t.getName();
    this.enabled=t.isEnabled();
    this.expandable=t.isExpandable();
    notifyObservers(this);
  }

  public boolean equals(Target t)
  {
    if(getHash().equals(t.getHash()))
      return true;
    return false;
  }
}

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


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import android.os.Parcel;
import android.os.Parcelable;
import com.tnc.android.graphite.functions.GraphFunction;


public class Target extends SimpleObservable<Target>
  implements Parcelable, Serializable, Cloneable
{
  private static final long serialVersionUID=7280880856241072365L;
  private int id=-1;
  private String hash="";
  private String name="";
  private boolean enabled=false;
  private boolean expandable=false;
  private Stack<GraphFunction> functions=new Stack<GraphFunction>();

  //Not persistent variables
  private boolean placeholder=false;
  private boolean loaded=true;

  public Target()
  {
  }
  
  public Target(Parcel in)
  {
    readFromParcel(in);
  }
  
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

  public void addFunction(GraphFunction function)
  {
    functions.push(function);
  }
  public void removeFunction()
  {
    if(!functions.empty())
    {
      functions.pop();
    }
  }
  public void removeAllFunctions()
  {
    functions.removeAllElements();
  }
  public Stack<GraphFunction> getFunctions()
  {
    if(null==functions)
    {
      functions=new Stack<GraphFunction>();
    }
    return functions;
  }
  public void setFunctions(Stack<GraphFunction> functions)
  {
    this.functions=functions;
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
    t.setFunctions((Stack<GraphFunction>)functions.clone());
    return t;
  }

  synchronized public void consume(Target t)
  {
    this.id=t.getId();
    this.name=t.getName();
    this.enabled=t.isEnabled();
    this.expandable=t.isExpandable();
    this.functions=t.getFunctions();
    notifyObservers(this);
  }
  
  @Override
  public boolean equals(Object o)
  {
    if(o instanceof Target
      && getName().equals(((Target)o).getName()))
    {
      return true;
    }
    return false;
  }
  
  public String getFullName()
  {
    String fullName=name;
    for(GraphFunction func : getFunctions())
    {
      fullName=func.apply(fullName);
    }
    return fullName;
  }
  
  @Override
  public String toString()
  {
    return getName();
  }
  
  @Override
  public int describeContents()
  {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags)
  {
    out.writeInt(id);
    out.writeString(hash);
    out.writeString(name);
    out.writeBooleanArray(new boolean[]{enabled, expandable, placeholder, loaded});
    out.writeArray(getFunctions().toArray());
  }
  
  private void readFromParcel(Parcel in)
  {
    id=in.readInt();
    hash=in.readString();
    name=in.readString();
    boolean[] boolArray=new boolean[4];
    in.readBooleanArray(boolArray);
    enabled=boolArray[0];
    expandable=boolArray[1];
    placeholder=boolArray[2];
    loaded=boolArray[3];
    List<Object> funcList=Arrays.asList(in.readArray(
      GraphFunction.class.getClassLoader()));
    for(Object obj : funcList)
    {
      functions.push((GraphFunction)obj);
    }
  }
  
  public static final Parcelable.Creator<Target> CREATOR=new Creator<Target>()
  {
    public Target createFromParcel(Parcel source)
    {
      return new Target(source);
    }

    public Target[] newArray(int size)
    {
      return new Target[size];
    }
  };
}

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
import java.util.ArrayList;
import java.util.Calendar;


public class GraphData implements Serializable, Cloneable
{
  private static final long serialVersionUID=-8859412917355254945L;
  private ArrayList<Target> targets;
  private Calendar intervalFrom;
  private Calendar intervalTo;
  private RecentRange range;
  private String name;

  public ArrayList<Target> getTargets()
  {
    return targets;
  }
  public void setTargets(ArrayList<Target> targets)
  {
    this.targets=targets;
  }
  
  public Calendar getIntervalFrom()
  {
    return intervalFrom;
  }
  public void setIntervalFrom(Calendar intervalFrom)
  {
    this.intervalFrom=intervalFrom;
  }
  
  public Calendar getIntervalTo()
  {
    return intervalTo;
  }
  public void setIntervalTo(Calendar intervalTo)
  {
    this.intervalTo=intervalTo;
  }
  
  public RecentRange getRange()
  {
    return range;
  }
  public void setRange(RecentRange range)
  {
    this.range=range;
  }
  
  public String getName()
  {
    return name;
  }
  public void setName(String name)
  {
    this.name=name;
  }
  
  public GraphData clone()
  {
    GraphData gd=new GraphData();
    gd.setName(name.toString());
    gd.setTargets((ArrayList<Target>)targets.clone());
    if(null!=intervalFrom)
    {
      gd.setIntervalFrom((Calendar)intervalFrom.clone());
    }
    if(null!=intervalTo)
    {
      gd.setIntervalTo((Calendar)intervalTo.clone());
    }
    if(null!=range)
    {
      gd.setRange(range.clone());
    }
    return gd;
  }
}

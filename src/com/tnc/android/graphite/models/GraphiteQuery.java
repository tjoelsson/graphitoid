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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;


public class GraphiteQuery
{
  private ArrayList<String> targets;
  private String range="";
  private long from=-1;
  private long until=-1;
  private int width=0;
  private int height=0;
  private String template=null;
  private String title=null;
  private String vTitle=null;
  private int lineWidth;
  private int yMin;
  private int yMax;
  private boolean graphOnly;
  private boolean hideLegend;
  private boolean hideGrid;
  private boolean hideAxes;
  private String lineMode=null;
  private String areaMode=null;
  private ArrayList<String> colorList;

  public GraphiteQuery()
  {
    this.targets=new ArrayList<String>();
    this.colorList=new ArrayList<String>();
  }

  public String getParamString()
  {
    return getParamString(false);
  }

  public String getParamString(boolean rawData)
  {
    StringBuffer paramString=new StringBuffer();

    // Targets
    for(Iterator<String> i=this.targets.iterator(); i.hasNext();)
    {
      String target=(String)i.next();
      paramString.append("target=");
      paramString.append(target);
      if(i.hasNext())
        paramString.append("&");
    }

    // Width and height
    if(0!=this.width)
    {
      paramString.append("&width="+this.width);
    }
    if(0!=this.height)
    {
      paramString.append("&height="+this.height);
    }

    if(!this.range.equals(""))
    {
      paramString.append("&from=-"+this.range);
    }

    if(0<=this.from)
    {
      paramString.append("&from="+this.from);
    }

    if(0<=this.until)
    {
      paramString.append("&until="+this.until);
    }

    // Raw data option
    if(rawData)
      paramString.append("&rawData=true");
    return paramString.toString();
  }

  public void addTarget(String target)
  {
    this.targets.add(target);
  }

  public void removeTarget(String target)
  {
    this.targets.remove(target);
  }

  // Getters and setters
  public ArrayList<String> getTargets()
  {
    return targets;
  }

  public String getRange()
  {
    return range;
  }

  public long getFrom()
  {
    return from;
  }

  public Calendar getFromCalendar()
  {
    Calendar c=Calendar.getInstance();
    c.add(Calendar.HOUR, -24);
    if(0<=from)
    {
      c.setTime(new Date(from*1000));
    }
    return c;
  }

  public long getUntil()
  {
    return until;
  }

  public Calendar getUntilCalendar()
  {
    Calendar c=Calendar.getInstance();
    if(0<=until)
    {
      c.setTime(new Date(until*1000));
    }
    return c;
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  }

  public String getTemplate()
  {
    return template;
  }

  public String getTitle()
  {
    return title;
  }

  public String getvTitle()
  {
    return vTitle;
  }

  public int getLineWidth()
  {
    return lineWidth;
  }

  public int getyMin()
  {
    return yMin;
  }

  public int getyMax()
  {
    return yMax;
  }

  public boolean isGraphOnly()
  {
    return graphOnly;
  }

  public boolean isHideLegend()
  {
    return hideLegend;
  }

  public boolean isHideGrid()
  {
    return hideGrid;
  }

  public boolean isHideAxes()
  {
    return hideAxes;
  }

  public String getLineMode()
  {
    return lineMode;
  }

  public String getAreaMode()
  {
    return areaMode;
  }

  public ArrayList<String> getColorList()
  {
    return colorList;
  }

  public void setTargets(ArrayList<String> targets)
  {
    this.targets=targets;
  }

  public void setRange(String range)
  {
    this.range=range;
  }

  public void setFrom(long from)
  {
    this.from=from;
  }

  public void setFromCalendar(Calendar c)
  {
    from=c.getTime().getTime()/1000;
  }

  public void setUntil(long until)
  {
    this.until=until;
  }

  public void setUntilCalendar(Calendar c)
  {
    until=c.getTime().getTime()/1000;
  }

  public void setWidth(int width)
  {
    this.width=width;
  }

  public void setHeight(int height)
  {
    this.height=height;
  }

  public void setTemplate(String template)
  {
    this.template=template;
  }

  public void setTitle(String title)
  {
    this.title=title;
  }

  public void setvTitle(String vTitle)
  {
    this.vTitle=vTitle;
  }

  public void setLineWidth(int lineWidth)
  {
    this.lineWidth=lineWidth;
  }

  public void setyMin(int yMin)
  {
    this.yMin=yMin;
  }

  public void setyMax(int yMax)
  {
    this.yMax=yMax;
  }

  public void setGraphOnly(boolean graphOnly)
  {
    this.graphOnly=graphOnly;
  }

  public void setHideLegend(boolean hideLegend)
  {
    this.hideLegend=hideLegend;
  }

  public void setHideGrid(boolean hideGrid)
  {
    this.hideGrid=hideGrid;
  }

  public void setHideAxes(boolean hideAxes)
  {
    this.hideAxes=hideAxes;
  }

  public void setLineMode(String lineMode)
  {
    this.lineMode=lineMode;
  }

  public void setAreaMode(String areaMode)
  {
    this.areaMode=areaMode;
  }

  public void setColorList(ArrayList<String> colorList)
  {
    this.colorList=colorList;
  }
}

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

package com.tnc.android.graphite.controllers;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;
import com.tnc.android.graphite.GraphiteApp;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.models.DrawableGraph;
import com.tnc.android.graphite.models.GraphData;
import com.tnc.android.graphite.models.GraphiteQuery;
import com.tnc.android.graphite.models.RecentRange;
import com.tnc.android.graphite.models.Target;
import com.tnc.android.graphite.utils.GraphStorage;
import com.tnc.android.graphite.utils.GraphiteConnection;


public class GraphController extends Controller
{
  @SuppressWarnings("unused")
  private static final String TAG=GraphController.class.getSimpleName();
  private HandlerThread workerThread;
  private Handler workerHandler;

  public static final int MESSAGE_PLOT_GRAPH=1;
  public static final int MESSAGE_VIEW_READY=2;
  public static final int MESSAGE_START_LOADING=3;
  public static final int MESSAGE_STOP_LOADING=4;
  public static final int MESSAGE_RELOAD=5;
  public static final int MESSAGE_FAIL_GO_BACK=6;
  public static final int MESSAGE_FAIL_STAY=7;
  public static final int MESSAGE_CONFIG_UPDATE=8;
  public static final int MESSAGE_START=9;
  public static final int MESSAGE_STOP=10;
  public static final int MESSAGE_AUTO_REFRESH_DIALOG=11;
  public static final int MESSAGE_SET_AUTO_REFRESH=12;
  public static final int MESSAGE_SAVE_GRAPH=13;
  public static final int MESSAGE_NOTIFY_SAVED=14;

  private String serverUrl;
  private DrawableGraph model;
  private List<Target> targets;
  private Calendar intervalFrom=null;
  private Calendar intervalTo=null;
  private RecentRange range=null;
  private boolean graphDisplayed=false;
  private int autoRefreshInterval=0;
  private int[] autoRefreshValues;

  public GraphController(DrawableGraph model)
  {
    this.model=model;
    setPrefs();
    targets=new ArrayList<Target>();
    autoRefreshValues=GraphiteApp.getContext().getResources()
      .getIntArray(R.array.auto_refresh_values);
  }

  @Override
  public void dispose()
  {
    super.dispose();
    workerThread.getLooper().quit();
  }

  public boolean handleMessage(int what, Object data)
  {
    switch(what)
    {
      case MESSAGE_RELOAD:
        // TODO use hash to only remove current graph
        GraphiteApp.getInstance().getGraphHolder().clear();
        graphDisplayed=false;
        plotGraph();
        return true;
      case MESSAGE_VIEW_READY:
        Bundle extras=(Bundle)data;
        targets=(ArrayList)extras.getParcelableArrayList("targets");
        intervalFrom=(Calendar)extras.getSerializable("from");
        intervalTo=(Calendar)extras.getSerializable("to");
        range=extras.getParcelable("range");
        return true;
      case MESSAGE_CONFIG_UPDATE:
        // TODO: use stored graph even if auto-refresh is enabled
        plotGraph();
        return true;
      case MESSAGE_STOP:
        workerHandler.removeCallbacksAndMessages(null);
        workerThread.interrupt();
        workerThread.quit();
        return true;
      case MESSAGE_START:
        workerThread=new HandlerThread("Graph Worker Thread");
        workerThread.start();
        workerHandler=new Handler(workerThread.getLooper());
        plotGraph();
        return true;
      case MESSAGE_AUTO_REFRESH_DIALOG:
        for(int i = 0; i < autoRefreshValues.length; ++i)
        {
          if(autoRefreshValues[i] == autoRefreshInterval)
          {
            notifyOutboxHandlers(MESSAGE_AUTO_REFRESH_DIALOG, 0, 0, i);
            return true;
          }
        }
      case MESSAGE_SET_AUTO_REFRESH:
        autoRefreshInterval=autoRefreshValues[(Integer)data];
        if(0<autoRefreshInterval)
        {
          plotGraph(autoRefreshInterval);
        }
        return true;
      case MESSAGE_SAVE_GRAPH:
        final String graphName = (String)data;
        workerHandler.post(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              GraphData graphData=new GraphData();
              graphData.setTargets(targets);
              graphData.setIntervalFrom(intervalFrom);
              graphData.setIntervalTo(intervalTo);
              graphData.setRange(range);
              graphData.setName(graphName);
              GraphStorage.getInstance().storeGraph(graphData);
              notifyOutboxHandlers(MESSAGE_NOTIFY_SAVED, 0, 0, null);
            }
            catch(Exception e)
            {
              e.printStackTrace();
              notifyOutboxHandlers(MESSAGE_FAIL_STAY, 0, 0, e);
            }
          }
        });
        return true;
    }
    return false;
  }
  
  private void plotGraph()
  {
    plotGraph(0);
  }
  
  private void plotGraph(int delay)
  {
    if(!graphDisplayed)
    {
      notifyOutboxHandlers(MESSAGE_START_LOADING, 0, 0, null);
    }
    workerHandler.removeCallbacksAndMessages(null);
    workerHandler.postDelayed(new PlotRunnable(), delay);
  }
  
  private void setPrefs()
  {
    SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(
      GraphiteApp.getContext());
    serverUrl=prefs.getString("server_url", "");
  }

  class PlotRunnable implements Runnable
  {
    @Override
    public void run()
    {
      if(workerThread.isInterrupted())
      {
        return;
      }
      if(0<autoRefreshInterval)
      {
        workerHandler.postDelayed(new PlotRunnable(), autoRefreshInterval);
      }
      GraphiteQuery query=new GraphiteQuery();
      for(Target t : targets)
      {
        query.addTarget(t.getName());
      }
      if(null!=range)
      {
        query.setRange("" + range.getValue() + range.getUnit());
      }
      if(null!=intervalFrom&&null!=intervalTo)
      {
        query.setFromCalendar(intervalFrom);
        query.setUntilCalendar(intervalTo);
      }
      WindowManager wm=(WindowManager)GraphiteApp.getContext().getSystemService(
        Context.WINDOW_SERVICE);
      Display display=wm.getDefaultDisplay();
      
      int width;
      int height;
//      if(android.os.Build.VERSION.SDK_INT >= 13)
//      {
//        Point size=new Point();
//        display.getSize(size);
//        width=size.x;
//        height=size.y;
//      }
//      else
//      {
      width=display.getWidth();
      height=display.getHeight();
//      }
      query.setWidth(width);
      query.setHeight(height);
      
      String paramString=query.getParamString();
      
      // Get from memory
      DrawableGraph graph=GraphiteApp.getInstance().getGraphHolder().get(
        (serverUrl+paramString).hashCode()
      );
      if(null!=graph)
      {
        long cutoff=60000000000L; // One minute in nanoseconds
        long autoCutoff=60000L; // One minute in milliseconds
        if(System.nanoTime()-graph.getTimestamp()>=cutoff
          ||(0<autoRefreshInterval&&autoRefreshInterval<autoCutoff))
        {
          graph=null;
        }
      }
      if(null==graph)
      {
        // Get from server
        try
        {
          graph=GraphiteConnection.getGraph(
            serverUrl,
            paramString
          );
        }
        catch(Exception e)
        {
          e.printStackTrace();
          notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
          notifyOutboxHandlers(MESSAGE_FAIL_GO_BACK, 0, 0, e);
        }
      }
      
      notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
      
      if(null!=graph)
      {
        model.consume(graph);
        GraphiteApp.getInstance().getGraphHolder().put(
          (serverUrl+paramString).hashCode(), graph);
        notifyOutboxHandlers(MESSAGE_PLOT_GRAPH, 0, 0, null);
        graphDisplayed=true;
      }
    }
  }
}

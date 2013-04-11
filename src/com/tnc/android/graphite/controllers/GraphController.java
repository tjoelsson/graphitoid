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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import com.tnc.android.graphite.GraphiteApp;
import com.tnc.android.graphite.models.Graph;
import com.tnc.android.graphite.models.GraphiteQuery;
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

  private String serverUrl;
  private Graph model;
  private ArrayList<String> targetStrings;
  private Calendar intervalFrom=null;
  private Calendar intervalTo=null;

  public GraphController(Graph model)
  {
    workerThread=new HandlerThread("Graph Worker Thread");
    workerThread.start();
    workerHandler=new Handler(workerThread.getLooper());

    this.model=model;
    setPrefs();
    targetStrings=new ArrayList<String>();
  }

  @Override
  public void dispose()
  {
    super.dispose();
    workerThread.getLooper().quit();
  }

  private void plotGraph()
  {
    notifyOutboxHandlers(MESSAGE_START_LOADING, 0, 0, null);
    workerHandler.post(new Runnable() {
      @Override
      public void run()
      {
        GraphiteQuery query=new GraphiteQuery();
        for(String str : targetStrings)
        {
          query.addTarget(str);
        }
        if(null!=intervalFrom&&null!=intervalTo)
        {
          query.setFromCalendar(intervalFrom);
          query.setUntilCalendar(intervalTo);
        }
        String paramString=query.getParamString(true);
        // Get from memory
        Graph graph=GraphiteApp.getInstance().getGraphHolder().get(
          (serverUrl+paramString).hashCode()
          );
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
        if(null==graph)
        {
          // TODO error handling
          notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
          System.err.println("Failed getting graph data");
        }
        else
        {
          model.consume(graph);
          GraphiteApp.getInstance().getGraphHolder().put(
            (serverUrl+paramString).hashCode(),
            graph
            );
          notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
          notifyOutboxHandlers(MESSAGE_PLOT_GRAPH, 0, 0, null);
        }
      }
    });
  }

  public boolean handleMessage(int what, Object data)
  {
    switch(what)
    {
      case MESSAGE_RELOAD:
        // TODO use hash to only remove current graph
        GraphiteApp.getInstance().getGraphHolder().clear();
        plotGraph();
        return true;
      case MESSAGE_VIEW_READY:
        Bundle extras=(Bundle)data;
        targetStrings=extras.getStringArrayList("targets");
        intervalFrom=(Calendar)extras.getSerializable("from");
        intervalTo=(Calendar)extras.getSerializable("to");
        plotGraph();
        return true;
    }
    return false;
  }

  private void setPrefs()
  {
    SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(
      GraphiteApp.getContext());
    serverUrl=prefs.getString("server_url", "");
  }
}

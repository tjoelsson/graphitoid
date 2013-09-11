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
import java.util.LinkedList;
import java.util.List;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import com.tnc.android.graphite.GraphiteApp;
import com.tnc.android.graphite.daos.TargetDao;
import com.tnc.android.graphite.models.GraphData;
import com.tnc.android.graphite.models.RecentRange;
import com.tnc.android.graphite.models.Target;
import com.tnc.android.graphite.utils.GraphStorage;
import com.tnc.android.graphite.utils.GraphiteConnection;


public class TargetsController extends Controller
  implements OnSharedPreferenceChangeListener
{
  @SuppressWarnings("unused")
  private static final String TAG=TargetsController.class.getSimpleName();
  private HandlerThread workerThread;
  private Handler workerHandler;

  public static final int MESSAGE_GET_TARGETS=1;
  public static final int MESSAGE_MODEL_NEW=2;
  public static final int MESSAGE_MODEL_UPDATED=3;
  public static final int MESSAGE_RELOAD_TARGETS=4;
  public static final int MESSAGE_UPDATE_ENABLED=5;
  public static final int MESSAGE_START_LOADING=6;
  public static final int MESSAGE_STOP_LOADING=7;
  public static final int MESSAGE_FAIL_GO_BACK=8;
  public static final int MESSAGE_FAIL_STAY=9;
  public static final int MESSAGE_SAVED_GRAPH=10;
  public static final int MESSAGE_DISPLAY_SAVED=11;
  public static final int MESSAGE_SELECT_SAVED=12;
  public static final int MESSAGE_DATE_TIME=13;
  public static final int MESSAGE_DATE_TIME_FROM=14;
  public static final int MESSAGE_DATE_TIME_TO=15;
  public static final int MESSAGE_RECENT_RANGE=16;
  public static final int MESSAGE_SET_RECENT_RANGE=17;
  public static final int MESSAGE_PLOT_GRAPH=18;
  
  final private int RANGE_TYPE_NONE=0;
  final private int RANGE_TYPE_RECENT=1;
  final private int RANGE_TYPE_DATES=2;
  
  private String serverUrl;
  private String targetFilter;
  private Calendar intervalFrom=null;
  private Calendar intervalTo=null;
  private RecentRange recentRange=null;
  private int currentRangeType=RANGE_TYPE_NONE;
  private ArrayList<Target> model;

  public ArrayList<Target> getModel()
  {
    return model;
  }

  public TargetsController(ArrayList<Target> model)
  {
    this.model=model;
    workerThread=new HandlerThread("Targets Worker Thread");
    workerThread.start();
    workerHandler=new Handler(workerThread.getLooper());

    setPrefs();
  }

  @Override
  public void dispose()
  {
    super.dispose();
    workerThread.getLooper().quit();

    SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(
      GraphiteApp.getContext());
    prefs.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public boolean handleMessage(int what, Object data)
  {
    switch(what)
    {
      case MESSAGE_GET_TARGETS:
        if(null!=data)
        {
          getTargets((Target)data);
        }
        else
        {
          getTargets();
        }
        return true;
      case MESSAGE_RELOAD_TARGETS:
        reloadTargets();
        return true;
      case MESSAGE_UPDATE_ENABLED:
        updateEnabled((Integer)data);
        return true;
      case MESSAGE_SAVED_GRAPH:
        workerHandler.post(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              LinkedList<GraphData> graphs=GraphStorage.getInstance().getGraphs();
              CharSequence[] graphList=new String[graphs.size()];
              for(int i=0;i<graphs.size();++i)
              {
                graphList[i]=graphs.get(i).getName();
              }
              notifyOutboxHandlers(MESSAGE_SAVED_GRAPH, 0, 0, graphList);
            }
            catch(Exception e)
            {
              e.printStackTrace();
              notifyOutboxHandlers(MESSAGE_FAIL_STAY, 0, 0, e);
            }
          }
        });
        return true;
      case MESSAGE_SELECT_SAVED:
        final Integer which=(Integer)data;
        workerHandler.post(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              GraphData graph=GraphStorage.getInstance().getGraphs().get(which);
              List<Target> savedTargets=graph.getTargets();
              intervalFrom=graph.getIntervalFrom();
              intervalTo=graph.getIntervalTo();
              recentRange=graph.getRange();
              if(null!=intervalFrom)
              {
                currentRangeType=RANGE_TYPE_DATES;
              }
              if(null!=recentRange)
              {
                currentRangeType=RANGE_TYPE_RECENT;
              }
              notifyOutboxHandlers(MESSAGE_DISPLAY_SAVED, 0, 0, savedTargets);
            }
            catch(Exception e)
            {
              e.printStackTrace();
              notifyOutboxHandlers(MESSAGE_FAIL_STAY, 0, 0, e);
            }
          }
        });
        return true;
      case MESSAGE_DATE_TIME:
        Calendar fromInput;
        if(null!=intervalFrom)
        {
          fromInput=(Calendar)intervalFrom.clone();
        }
        else
        {
          fromInput=Calendar.getInstance();
        }
        notifyOutboxHandlers(MESSAGE_DATE_TIME_FROM, 0, 0, fromInput);
        return true;
      case MESSAGE_DATE_TIME_FROM:
        intervalFrom=(Calendar)data;
        Calendar toInput;
        if(null!=intervalTo)
        {
          toInput=(Calendar)intervalTo.clone();
        }
        else
        {
          toInput=Calendar.getInstance();
        }
        notifyOutboxHandlers(MESSAGE_DATE_TIME_TO, 0, 0, toInput);
        return true;
      case MESSAGE_DATE_TIME_TO:
        intervalTo=(Calendar)data;
        currentRangeType=RANGE_TYPE_DATES;
        return true;
      case MESSAGE_RECENT_RANGE:
        RecentRange range=recentRange;
        if(null==range)
        {
          range=new RecentRange(24, "Hours");
        }
        notifyOutboxHandlers(MESSAGE_RECENT_RANGE, 0, 0, range);
        return true;
      case MESSAGE_SET_RECENT_RANGE:
        recentRange=(RecentRange)data;
        currentRangeType=RANGE_TYPE_RECENT;
        return true;
      case MESSAGE_PLOT_GRAPH:
        startPlotActivity((ArrayList<Target>)data);
        return true;
    }
    return false;
  }

  private void updateEnabled(final int id)
  {
    workerHandler.post(new Runnable() {
      @Override
      public void run()
      {
        synchronized(model)
        {
          TargetDao dao=new TargetDao();
          dao.update(model.get(id)); // not same as Target ID value
        }
      }
    });
  }

  private void getTargets()
  {
    getTargets(null);
  }

  private void getTargets(final Target baseTarget)
  {
    if(null!=baseTarget)
    {
      notifyOutboxHandlers(MESSAGE_START_LOADING, 0, 0, null);
      workerHandler.post(new Runnable()
      {
        @Override
        public void run()
        {
          // Load children of baseTarget
          ArrayList<Target> targets;
          try
          {
            targets=GraphiteConnection.getTargets(
              serverUrl,
              baseTarget.getName()+".*",
              1
              );
          }
          catch(Exception e)
          {
            e.printStackTrace();
            notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
            notifyOutboxHandlers(MESSAGE_FAIL_STAY, 0, 0, e);
            return;
          }
          synchronized(model)
          {
            int idx=model.indexOf(baseTarget);
            model.get(idx).setLoaded(true);
            idx++;
            for(int i=0; i<targets.size(); ++i)
            {
              model.add(idx+i, targets.get(i));
            }
            persistModel();
          }
          notifyOutboxHandlers(MESSAGE_MODEL_UPDATED, targets.size(), 0,
            baseTarget);
          notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
        }
      });
      return;
    }
    workerHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        TargetDao dao=new TargetDao();
        ArrayList<Target> targets=dao.getAll();
        String hash=String.valueOf((serverUrl+targetFilter).hashCode());
        if(0<targets.size()&&targets.get(0).getHash().equals(hash))
        {
          synchronized(model)
          {
            while(model.size()>0)
            {
              model.remove(0);
            }
            for(Target target : targets)
            {
              model.add(target);
            }
          }
          notifyOutboxHandlers(MESSAGE_MODEL_NEW, 0, 0, null);
        }
        else
        {
          // Force reload
          reloadTargets();
        }
      }
    });
  }

  private void reloadTargets()
  {
    notifyOutboxHandlers(MESSAGE_START_LOADING, 0, 0, null);
    workerHandler.post(new Runnable() {
      @Override
      public void run()
      {
        ArrayList<Target> targets;
        try
        {
          targets=GraphiteConnection.getTargets(
            serverUrl,
            targetFilter,
            1 // TODO make configurable
          );
        }
        catch(Exception e)
        {
          e.printStackTrace();
          notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
          notifyOutboxHandlers(MESSAGE_FAIL_GO_BACK, 0, 0, e);
          return;
        }
        synchronized(model)
        {
          while(model.size()>0)
          {
            model.remove(0);
          }
          for(Target target : targets)
          {
            model.add(target);
          }
          persistModel();
        }
        notifyOutboxHandlers(MESSAGE_STOP_LOADING, 0, 0, null);
        notifyOutboxHandlers(MESSAGE_MODEL_NEW, 0, 0, null);
      }
    });
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
    String key)
  {
    setPrefs();
  }

  private void setPrefs()
  {
    SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(
      GraphiteApp.getContext());
    prefs.registerOnSharedPreferenceChangeListener(this);
    serverUrl=prefs.getString("server_url", "");
    targetFilter=prefs.getString("target_filter", "*");
  }

  private void persistModel()
  {
    TargetDao dao=new TargetDao();
    dao.deleteAll();
    for(Target target : model)
    {
      target.setId(-1);
      long id=dao.insert(target);
      target.setId((int)id);
    }
  }
  
  private void startPlotActivity(final ArrayList<Target> selected)
  {
    workerHandler.post(new Runnable()
    {
      @Override
      public void run()
      {
        Bundle plotBundle=new Bundle();
        plotBundle.putParcelableArrayList("targets", selected);
        switch(currentRangeType)
        {
          case RANGE_TYPE_RECENT:
            plotBundle.putParcelable("range", recentRange);
            break;
          case RANGE_TYPE_DATES:
            plotBundle.putSerializable("from", intervalFrom);
            plotBundle.putSerializable("to", intervalTo);
            break;
        }
        notifyOutboxHandlers(MESSAGE_PLOT_GRAPH, 0, 0, plotBundle);
      }
    });
  }
}

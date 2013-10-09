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

package com.tnc.android.graphite.activities;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeViewList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.controllers.TargetsController;
import com.tnc.android.graphite.fragments.IntervalDialogFragment;
import com.tnc.android.graphite.lists.TargetsTreeAdapter;
import com.tnc.android.graphite.models.RecentRange;
import com.tnc.android.graphite.models.Target;
import com.tnc.android.graphite.utils.RecentRangeDialog;
import com.tnc.android.graphite.utils.SwipeGestureListener;
import com.tnc.android.graphite.utils.UserNotification;
import com.tnc.android.graphite.views.SwipeView;


public class TargetsActivity extends FragmentActivity implements Handler.Callback
{
  final Activity me=this;
  private ArrayList<Target> targets;
  private TargetsController controller;
  private ProgressDialog dialog;
  private InMemoryTreeStateManager<Target> manager=null;
  private TreeViewList treeView;
  private TargetsTreeAdapter adapter;
  private List<Target> selected=new ArrayList<Target>();
  
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.targets_treeview);

    targets=new ArrayList<Target>();
    controller=new TargetsController(targets);
    controller.addOutboxHandler(new Handler(this));

    manager=new InMemoryTreeStateManager<Target>();
    treeView=(TreeViewList)findViewById(R.id.mainTreeView);

    SwipeGestureListener sgl=new SwipeGestureListener()
    {
      @Override
      public void onRightSwipe()
      {
        me.finish();
      }
      @Override
      public void onLeftSwipe()
      {
        controller.handleMessage(TargetsController.MESSAGE_PLOT_GRAPH, selected);
      }
    };
    final GestureDetector gestureDetector=new GestureDetector(sgl);

    SwipeView mainView=(SwipeView)findViewById(R.id.mainView);
    mainView.setGestureDetector(gestureDetector);

    getTargets();
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    controller.dispose();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
    Intent data)
  {
    if(requestCode==TargetsController.ACTIVITY_SAVED_GRAPHS)
    {
      if(resultCode==RESULT_OK)
      {
        controller.handleMessage(TargetsController.MESSAGE_SELECT_SAVED, data);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.targets_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.targets_menu_recent_range:
        controller.handleMessage(TargetsController.MESSAGE_RECENT_RANGE);
        break;
      case R.id.targets_menu_datetime:
        controller.handleMessage(TargetsController.MESSAGE_DATE_TIME);
        break;
      case R.id.targets_menu_saved:
        Intent intent=new Intent(this, SavedActivity.class);
        startActivityForResult(intent, TargetsController.ACTIVITY_SAVED_GRAPHS);
        break;
      case R.id.targets_menu_reload:
        controller.handleMessage(TargetsController.MESSAGE_RELOAD_TARGETS);
        break;
      case R.id.targets_menu_clear:
        selected.clear();
        manager.refresh();
        adapter.notifyDataSetChanged();
        break;
      case R.id.targets_menu_plot:
        controller.handleMessage(TargetsController.MESSAGE_PLOT_GRAPH, selected);
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  private void getTargets()
  {
    controller.handleMessage(TargetsController.MESSAGE_GET_TARGETS);
  }

  public void getMoreTargets(Target target)
  {
    controller.handleMessage(TargetsController.MESSAGE_GET_TARGETS, target);
  }

  @Override
  public boolean handleMessage(final Message msg)
  {
    switch(msg.what)
    {
      case TargetsController.MESSAGE_MODEL_NEW:
        if(0==targets.size())
        {
          // No targets in model, go back to settings
          UserNotification.display(me, UserNotification.UPDATE_SETTINGS);
          this.finish();
          return true;
        }
        renderTargetTree();
        return true;
      case TargetsController.MESSAGE_MODEL_UPDATED:
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            // Updated target is passed here so don't have to update entire tree
            Target base=(Target)msg.obj;
            TreeBuilder<Target> builder=new TreeBuilder<Target>(manager);
            int idx=targets.indexOf(base)+1;
            for(int i=0; i<msg.arg1; ++i)
            {
              if(idx+i>=targets.size())
              {
                // Out of bounds, must have failed to load new targets
                UserNotification.display(me, UserNotification.UPDATE_SETTINGS);
                return;
              }
              Target newTarget=targets.get(idx+i);
              if(!newTarget.isExpandable())
              {
                builder.addRelation(base, newTarget);
              }
              else
              {
                newTarget.setLoaded(false);
                builder.addRelation(base, newTarget);
                Target placeholder=newTarget.clone();
                placeholder.setName(placeholder.getName()+"-placeholder");
                placeholder.setPlaceholder(true);
                builder.addRelation(newTarget, placeholder);
              }
            }
            adapter.notifyDataSetChanged();
          }
        });
        return true;
      case TargetsController.MESSAGE_START_LOADING:
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            dialog=ProgressDialog.show(me, "",
              "Loading. Please wait...", true);
          }
        });
        return true;
      case TargetsController.MESSAGE_STOP_LOADING:
        runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            dialog.cancel();
          }
        });
        return true;
      case TargetsController.MESSAGE_FAIL_GO_BACK:
        UserNotification.displayRaw(me, msg.obj.toString());
        this.finish();
        return true;
      case TargetsController.MESSAGE_FAIL_STAY:
        UserNotification.displayRaw(me, msg.obj.toString());
        return true;
      case TargetsController.MESSAGE_DISPLAY_SAVED:
        selected.clear();
        selected.addAll((List<Target>)msg.obj);
        manager.refresh();
        adapter.notifyDataSetChanged();
        return true;
      case TargetsController.MESSAGE_DATE_TIME_FROM:
        IntervalDialogFragment fromDialog=new IntervalDialogFragment((Calendar)msg.obj,
          getString(R.string.interval_from_header), new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate)
            {
              controller.handleMessage(TargetsController.MESSAGE_DATE_TIME_FROM, selectedDate);
            }
          });
        fromDialog.show(getSupportFragmentManager(), "from_dialog");
        return true;
      case TargetsController.MESSAGE_DATE_TIME_TO:
        IntervalDialogFragment toDialog=new IntervalDialogFragment((Calendar)msg.obj,
          getString(R.string.interval_to_header), new DateSlider.OnDateSetListener() {
            public void onDateSet(DateSlider view, Calendar selectedDate)
            {
              controller.handleMessage(TargetsController.MESSAGE_DATE_TIME_TO, selectedDate);
            }
          });
        toDialog.show(getSupportFragmentManager(), "to_dialog");
        return true;
      case TargetsController.MESSAGE_RECENT_RANGE:
        RecentRangeDialog rangeDialog=new RecentRangeDialog(this,
          (RecentRange)msg.obj, new RecentRangeDialog.OnRangeSetListener()
        {
          public void onRangeSet(RecentRange range)
          {
            controller.handleMessage(TargetsController.MESSAGE_SET_RECENT_RANGE, range);
          }
        });
        rangeDialog.show();
        return true;
      case TargetsController.MESSAGE_PLOT_GRAPH:
        Intent intent=new Intent(this, GraphActivity.class);
        intent.putExtras((Bundle)msg.obj);
        startActivity(intent);
        return true;
    }
    return false;
  }

  private int calcLevel(Target target)
  {
    int lvl=0;
    for(char c : target.getName().toCharArray())
    {
      if('.'==c)
        lvl++;
    }
    return lvl;
  }
  
  private void renderTargetTree()
  {
    runOnUiThread(new Runnable() {
      @Override
      public void run()
      {
        manager.clear();
        manager.setVisibleByDefault(false);
        TreeBuilder<Target> builder=new TreeBuilder<Target>(manager);

        int base=calcLevel(targets.get(0));
        for(int i=0; i<targets.size(); ++i)
        {
          Target target=targets.get(i);
          int level=calcLevel(target);

          if(target.isExpandable()&&(i+1==targets.size()||
            calcLevel(targets.get(i+1))<=level))
          {
            // The children are not yet loaded
            target.setLoaded(false);
            builder.sequentiallyAddNextNode(target, level-base);
            Target placeholder=target.clone();
            placeholder.setName(placeholder.getName()+"-placeholder");
            placeholder.setPlaceholder(true);
            builder.sequentiallyAddNextNode(placeholder, level-base+1);
          }
          else
          {
            builder.sequentiallyAddNextNode(target, level-base);
          }
        }
        adapter=new TargetsTreeAdapter(
          me,
          selected,
          manager,
          1 // Is 1 always ok?
        );

        treeView.setAdapter(adapter);
        treeView.setCollapsible(true);
        adapter.notifyDataSetChanged();
      }
    });
  }
}

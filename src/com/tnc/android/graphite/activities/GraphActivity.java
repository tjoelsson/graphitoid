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


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.controllers.GraphController;
import com.tnc.android.graphite.models.Graph;
import com.tnc.android.graphite.models.GraphiteChart;
import com.tnc.android.graphite.utils.ErrorMessage;
import com.tnc.android.graphite.utils.SwipeGestureListener;


public class GraphActivity extends Activity implements Handler.Callback, BaseActivity
{
  final static int DATE_TIME=104;

  private GraphController controller;
  private Graph model;

  private ProgressDialog dialog;
  private GestureDetector gestureDetector;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plain);

    model=new Graph();
    controller=new GraphController(model);
    controller.addOutboxHandler(new Handler(this));
    controller.handleMessage(
      GraphController.MESSAGE_VIEW_READY,
      this.getIntent().getExtras()
    );

    SwipeGestureListener sgl=new SwipeGestureListener(this);
    gestureDetector=new GestureDetector(sgl);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.graph_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.graph_menu_reload:
        controller.handleMessage(GraphController.MESSAGE_RELOAD);
        break;
      case R.id.graph_menu_targets:
        this.finish();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  @Override
  public boolean handleMessage(Message msg)
  {
    final Activity me=this;
    switch(msg.what)
    {
      case GraphController.MESSAGE_PLOT_GRAPH:
        runOnUiThread(new Runnable() {
          @Override
          public void run()
          {
            plotGraph();
          }
        });
        return true;
      case GraphController.MESSAGE_START_LOADING:
        runOnUiThread(new Runnable() {
          @Override
          public void run()
          {
            dialog=ProgressDialog.show(me, "",
              "Loading. Please wait...", true);
          }
        });
        return true;
      case GraphController.MESSAGE_STOP_LOADING:
        runOnUiThread(new Runnable() {
          @Override
          public void run()
          {
            dialog.cancel();
          }
        });
        return true;
      case GraphController.MESSAGE_FAIL_GO_BACK:
        ErrorMessage.displayRaw(me, msg.obj.toString());
        this.finish();
        return true;
    }
    return false;
  }

  private void plotGraph()
  {
    GraphiteChart chart=new GraphiteChart();
    View graphView=chart.createView(this, model);
    setContentView(graphView);

    graphView.setOnTouchListener(new View.OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event)
      {
        if(gestureDetector.onTouchEvent(event))
        {
          return true;
        }
        return false;
      }
    });
  }

  public void onLeftSwipe()
  {
  }

  public void onRightSwipe()
  {
    this.finish();
  }
}

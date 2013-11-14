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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.controllers.GraphController;
import com.tnc.android.graphite.controllers.TargetsController;
import com.tnc.android.graphite.models.DrawableGraph;
import com.tnc.android.graphite.utils.SwipeGestureListener;
import com.tnc.android.graphite.utils.UserNotification;


public class GraphActivity extends FragmentActivity implements Handler.Callback
{
  final static int DATE_TIME=104;

  private Activity me=this;
  private GraphController controller;
  private DrawableGraph model;
  private ProgressDialog dialog;
  private GestureDetector gestureDetector;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.plain);
    
    model = new DrawableGraph();
    controller=new GraphController(model);
    controller.addOutboxHandler(new Handler(this));
    controller.handleMessage(
      GraphController.MESSAGE_VIEW_READY,
      this.getIntent().getExtras()
    );

    SwipeGestureListener sgl=new SwipeGestureListener()
    {
      @Override
      public boolean onDown(MotionEvent e)
      {
        return true;
      }
      @Override
      public void onRightSwipe()
      {
        me.finish();
      }
    };
    gestureDetector=new GestureDetector(sgl);
    
    View listView=this.findViewById(R.id.plainView);
    listView.setOnTouchListener(new View.OnTouchListener()
    {
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

  @Override
  public void onStop()
  {
    super.onStop();
    controller.handleMessage(GraphController.MESSAGE_STOP);
  }
  
  @Override
  public void onStart()
  {
    super.onStart();
    controller.handleMessage(GraphController.MESSAGE_START);
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode,
    Intent data)
  {
    if(requestCode==GraphController.ACTIVITY_EDIT_GRAPHS)
    {
      if(resultCode==RESULT_OK)
      {
        controller.handleMessage(GraphController.MESSAGE_FINISHED_EDITING, data);
      }
    }
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
    // TODO move to controller
    switch(item.getItemId())
    {
      case R.id.graph_menu_reload:
        controller.handleMessage(GraphController.MESSAGE_RELOAD);
        break;
      case R.id.graph_menu_auto_refresh:
        controller.handleMessage(GraphController.MESSAGE_AUTO_REFRESH_DIALOG);
        break;
      case R.id.graph_menu_edit:
        Intent editIntent=new Intent(this, EditActivity.class);
        startActivityForResult(editIntent, GraphController.ACTIVITY_EDIT_GRAPHS);
        break;
      case R.id.graph_menu_save:
        final EditText input = new EditText(this);
        final AlertDialog saveDialog = new AlertDialog.Builder(this)
          .setTitle(R.string.save_dialog_title)
          .setView(input)
          .setPositiveButton(R.string.dialog_positive, null)
          .setNegativeButton(R.string.dialog_negative, null)
          .create();
        saveDialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
          @Override
          public void onShow(DialogInterface dialog)
          {
            Button b = saveDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(new View.OnClickListener()
            {
              @Override
              public void onClick(View view)
              {
                String value = input.getText().toString();
                if(!value.equals(""))
                {
                  controller.handleMessage(GraphController.MESSAGE_SAVE_GRAPH, value);
                  saveDialog.dismiss();
                }
              }
            });
          }
        });
        saveDialog.show();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration conf)
  {
    super.onConfigurationChanged(conf);
    cancelDialog();
    controller.handleMessage(GraphController.MESSAGE_CONFIG_UPDATE);
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
        cancelDialog();
        return true;
      case GraphController.MESSAGE_FAIL_GO_BACK:
        UserNotification.displayRaw(me, msg.obj.toString());
        this.finish();
        return true;
      case TargetsController.MESSAGE_FAIL_STAY:
        UserNotification.displayRaw(me, msg.obj.toString());
        return true;
      case GraphController.MESSAGE_AUTO_REFRESH_DIALOG:
        AlertDialog.Builder builder = new AlertDialog.Builder(me);
        builder.setTitle(getResources().getString(R.string.auto_refresh_dialog_title))
          .setSingleChoiceItems(R.array.auto_refresh_display,
            (Integer)msg.obj, new DialogInterface.OnClickListener()
          {
            public void onClick(DialogInterface dialog, int which)
            {
              controller.handleMessage(GraphController.MESSAGE_SET_AUTO_REFRESH, which);
              dialog.dismiss();
            }
          });
        Dialog autoRefreshDialog = builder.create();
        autoRefreshDialog.show();
        return true;
      case GraphController.MESSAGE_NOTIFY_SAVED:
        UserNotification.displayRaw(this, getString(R.string.graph_saved_message));
        return true;
    }
    return false;
  }

  private void cancelDialog()
  {
    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        if(null!=dialog)
        {
          dialog.cancel();
        }
      }
    });
  }

  private void plotGraph()
  {
    ImageView graphView = new ImageView(this);
    graphView.setImageDrawable(model.getImage());
    graphView.setScaleType(ImageView.ScaleType.FIT_XY);
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
}

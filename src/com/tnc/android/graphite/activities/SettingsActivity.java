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


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.controllers.SettingsController;
import com.tnc.android.graphite.utils.SwipeGestureListener;


public class SettingsActivity extends PreferenceActivity implements Handler.Callback, BaseActivity
{
  SettingsController controller;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    setContentView(R.layout.settings);

    controller=new SettingsController();
    controller.addOutboxHandler(new Handler(this));
    controller.handleMessage(SettingsController.MESSAGE_VIEW_READY, null);
    
    SwipeGestureListener sgl=new SwipeGestureListener(this);
    final GestureDetector gestureDetector=new GestureDetector(sgl);

    View listView=this.findViewById(android.R.id.list);
    listView.setOnTouchListener(new View.OnTouchListener() {
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
  public void onLeftSwipe()
  {
    Intent intent=new Intent(this, TargetsActivity.class);
    startActivity(intent);
  }

  @Override
  public void onRightSwipe()
  {
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.settings_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch(item.getItemId())
    {
      case R.id.settings_menu_targets:
        Intent intent=new Intent(this, TargetsActivity.class);
        startActivity(intent);
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }
  
  @Override
  public boolean handleMessage(final Message msg)
  {
    String value=(String)msg.obj;
    switch(msg.what)
    {
      case SettingsController.MESSAGE_UPDATE_HTTP_USER:
        EditTextPreference user=(EditTextPreference)getPreferenceScreen()
          .findPreference("http_user");
        if(value.equals(""))
        {
          user.setSummary(R.string.settings_http_user_summary);
        }
        else
        {
          user.setSummary(value);
        }
        break;
      case SettingsController.MESSAGE_UPDATE_HTTP_PASS:
        EditTextPreference pass=(EditTextPreference)getPreferenceScreen()
          .findPreference("http_pass");
        if(value.equals(""))
        {
          pass.setSummary(R.string.settings_http_pass_summary);
        }
        else
        {
          pass.setSummary(value.replaceAll(".", "*"));
        }
        break;
            
    }
    return false;
  }
}

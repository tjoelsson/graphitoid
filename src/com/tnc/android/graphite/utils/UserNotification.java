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

package com.tnc.android.graphite.utils;


import android.app.Activity;
import android.widget.Toast;
import com.tnc.android.graphite.GraphiteApp;
import com.tnc.android.graphite.R;


public class UserNotification
{
  public final static int UPDATE_SETTINGS=101;
  public final static int CONNECTION=102;

  public static void display(Activity act, int errCode)
  {
    GraphiteApp app=GraphiteApp.getInstance();
    String message="";
    switch(errCode)
    {
      case UPDATE_SETTINGS:
        message=app.getResources().getString(R.string.update_settings_error);
        break;
      case CONNECTION:
        message=app.getResources().getString(R.string.connection_error);
        break;
    }
    displayRaw(act, message);
  }

  public static void displayRaw(Activity act, final String msg)
  {
    final GraphiteApp app=GraphiteApp.getInstance();
    act.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        Toast.makeText(app.getBaseContext(), msg,
          Toast.LENGTH_LONG).show();
      }
    });
  }
}

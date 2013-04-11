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

package com.tnc.android.graphite;


import java.util.HashMap;
import android.app.Application;
import android.content.Context;
import com.tnc.android.graphite.models.DrawableGraph;


public class GraphiteApp extends Application
{
  private static GraphiteApp instance;
  private HashMap<Integer, DrawableGraph> graphHolder;

  public void onCreate()
  {
    super.onCreate();
    instance=this;
    graphHolder=new HashMap<Integer, DrawableGraph>();
  }

  public static GraphiteApp getInstance()
  {
    return instance;
  }

  public static Context getContext()
  {
    return instance.getApplicationContext();
  }

  public HashMap<Integer, DrawableGraph> getGraphHolder()
  {
    return graphHolder;
  }
}

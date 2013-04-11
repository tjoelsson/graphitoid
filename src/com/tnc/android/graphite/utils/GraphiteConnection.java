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


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.graphics.drawable.Drawable;
import com.tnc.android.graphite.models.DrawableGraph;
import com.tnc.android.graphite.models.Target;


public class GraphiteConnection
{
  final static String TARGETS_PARAM_STRING="/metrics/find/?format=treejson&query=";
  final static String GRAPH_PARAM_STRING="/render/?";

  private static String hash;

  public static ArrayList<Target> getTargets(String serverUrl,
    String targetFilter, int levels) throws Exception
  {
    ArrayList<Target> targets=new ArrayList<Target>();
    hash=String.valueOf((serverUrl+targetFilter).hashCode());
    getTargetsRecursive(targets, serverUrl, targetFilter, levels);

    return targets;
  }

  private static void getTargetsRecursive(ArrayList<Target> targets,
    String serverUrl, String targetFilter, int levels)
    throws Exception
  {
    BufferedReader reader=getReader(
      serverUrl+TARGETS_PARAM_STRING+targetFilter);

    String line;
    if((line=reader.readLine())!=null)
    {
      JSONTokener jTok=new JSONTokener(line);
      if(jTok.more())
      {
        JSONArray array=(JSONArray)jTok.nextValue();
        for(int i=0; i<array.length(); i++)
        {
          JSONObject obj=array.getJSONObject(i);
          String id=obj.getString("id");
          int exp=obj.getInt("expandable");
          Target t=new Target();
          t.setHash(hash);
          t.setName(id);
          if(1==exp)
          {
            t.setExpandable(true);
          }
          targets.add(t);
          if(1==exp&&1<levels)
          {
            // Recursively parse the branch
            getTargetsRecursive(targets, serverUrl, id+".*", levels-1);
          }
        }
      }
    }
  }

  public static DrawableGraph getGraph(String serverUrl, String ps)
    throws Exception
  {
    URL url=new URL(serverUrl+GRAPH_PARAM_STRING+ps);
    HttpURLConnection http=(HttpURLConnection)url.openConnection();
    http.setConnectTimeout(30000);
    http.setReadTimeout(30000);
    Drawable image=Drawable.createFromStream(http.getInputStream(), null);
    DrawableGraph dg=new DrawableGraph();
    dg.setImage(image);
    return dg;
  }
  
  private static BufferedReader getReader(String urlString)
    throws Exception
  {
    URL url=new URL(urlString);
    HttpURLConnection http=(HttpURLConnection)url.openConnection();
    http.setConnectTimeout(30000);
    http.setReadTimeout(30000);
    BufferedReader reader=new BufferedReader(
      new InputStreamReader(http.getInputStream()), 8192);
    return reader;
  }
}

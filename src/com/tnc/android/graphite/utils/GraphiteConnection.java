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
import java.util.List;
import java.util.StringTokenizer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.tnc.android.graphite.models.Graph;
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

  public static Graph getGraph(String serverUrl, String ps)
    throws Exception
  {
    BufferedReader reader=getReader(serverUrl+GRAPH_PARAM_STRING+ps);

    List<List<Double>> values=new ArrayList<List<Double>>();
    List<String> names=new ArrayList<String>();
    List<Integer> steps=new ArrayList<Integer>();
    String line;
    int from=-1;
    int to=-1;
    while((line=reader.readLine())!=null)
    {
      String[] lineArray=line.split("\\|");
      String[] infoArray=lineArray[0].split(",");
      String name=infoArray[0];
      if(0>from)
      {
        from=Integer.valueOf(infoArray[1]).intValue();
        to=Integer.valueOf(infoArray[2]).intValue();
      }
      List<Double> valueList=parseData(lineArray[1]);
      if(false==valueList.isEmpty()) {
        names.add(name);
        steps.add(Integer.valueOf(infoArray[3]));
        values.add(valueList);
      }
    }

    Graph g=new Graph();
    g.setValues(values);
    g.setNames(names);
    g.setSteps(steps);
    g.setFrom(from);
    g.setTo(to);

    return g;
  }

  private static List<Double> parseData(String dataString)
  {
    StringTokenizer st=new StringTokenizer(dataString, ",");
    List<Double> valueList=new ArrayList<Double>();
    while(st.hasMoreTokens())
    {
      String tok=st.nextToken();
      if(false==tok.equals("None"))
      {
        valueList.add(Double.valueOf(tok));
      }
    }
    return valueList;
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

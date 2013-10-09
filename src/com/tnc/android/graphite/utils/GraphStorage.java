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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import android.content.Context;
import com.tnc.android.graphite.GraphiteApp;
import com.tnc.android.graphite.models.GraphData;


public class GraphStorage
{
  final private static String FILE_NAME="saved_graphs.dat";
  
  private static GraphStorage _instance=null;
  private LinkedList<GraphData> _graphs=null;
  
  private GraphStorage()
  {
  }
  
  public static GraphStorage getInstance()
  {
    if(null==_instance)
    {
      _instance=new GraphStorage();
    }
    return _instance;
  }
  
  public void storeGraph(GraphData graph) throws IOException, ClassNotFoundException
  {
    _retrieveGraphs();
    _graphs.addFirst(graph);
    _saveGraphs();
  }
  
  public void deleteGraph(int position) throws IOException, ClassNotFoundException
  {
    _retrieveGraphs();
    _graphs.remove(position);
    _saveGraphs();
  }
  
  public LinkedList<GraphData> getGraphs() throws IOException, ClassNotFoundException
  {
    _retrieveGraphs();
    return _graphs;
  }
  
  private void _retrieveGraphs() throws IOException, ClassNotFoundException
  {
    if(null==_graphs)
    {
      String[] fileList=GraphiteApp.getContext().fileList();
      boolean exists=false;
      for(String filename : fileList)
      {
        if(filename.equals(FILE_NAME))
        {
          exists=true;
        }
      }
      if(!exists)
      {
        _graphs=new LinkedList<GraphData>();
      }
      else
      {
        ObjectInputStream ois=new ObjectInputStream(
          GraphiteApp.getContext().openFileInput(FILE_NAME));
        _graphs=(LinkedList<GraphData>)ois.readObject();
        ois.close();
      }
    }
  }
  
  private void _saveGraphs() throws IOException, ClassNotFoundException
  {
    if(null==_graphs)
    {
      _retrieveGraphs();
    }
    ObjectOutputStream oos=new ObjectOutputStream(
      GraphiteApp.getContext().openFileOutput(
        FILE_NAME, Context.MODE_PRIVATE));
    oos.writeObject(_graphs);
    oos.close();
  }
}

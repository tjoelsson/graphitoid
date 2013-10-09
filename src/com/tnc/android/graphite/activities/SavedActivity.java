/*
 * Copyright 2013 Tomas Joelsson
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
import java.util.LinkedList;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.fragments.SavedGraphsFragment;
import com.tnc.android.graphite.fragments.SavedGraphsFragment.OnItemSelectedListener;
import com.tnc.android.graphite.models.GraphData;
import com.tnc.android.graphite.utils.GraphStorage;
import com.tnc.android.graphite.utils.UserNotification;


public class SavedActivity extends FragmentActivity implements OnItemSelectedListener
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.saved_graphs);
    
    ArrayList<String> savedGraphs=new ArrayList<String>();
    try
    {
      LinkedList<GraphData> graphs=GraphStorage.getInstance().getGraphs();
      for(int i=0;i<graphs.size();++i)
      {
        savedGraphs.add(graphs.get(i).getName());
      }
      
      SavedGraphsFragment fragment=(SavedGraphsFragment)getSupportFragmentManager()
        .findFragmentById(R.id.fragment);
      
      if(fragment!=null&&fragment.isInLayout())
      {
        fragment.setContent(savedGraphs);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      UserNotification.displayRaw(this, e.toString());
      finish();
    }
  }

  @Override
  public void onSelect(int position)
  {
    try
    {
      GraphData graphData=GraphStorage.getInstance().getGraphs().get(position);
      Intent intent=this.getIntent();
      intent.putExtra("graph_data", graphData);
      this.setResult(RESULT_OK, intent);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      UserNotification.displayRaw(this, e.toString());
    }
    finish();
  }

  @Override
  public void onDelete(int position)
  {
    try
    {
      GraphStorage.getInstance().deleteGraph(position);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      UserNotification.displayRaw(this, e.toString());
    }
  }
}

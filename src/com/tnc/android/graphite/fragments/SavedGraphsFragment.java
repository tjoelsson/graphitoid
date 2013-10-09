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

package com.tnc.android.graphite.fragments;


import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import com.tnc.android.graphite.R;


public class SavedGraphsFragment extends ListFragment
{
  private OnItemSelectedListener listener;
  private ArrayAdapter<String> adapter;
  private ArrayList<String> graphs;
  
  public void setContent(ArrayList<String> graphs)
  {
    final ArrayList<String> savedGraphs=graphs;
    this.graphs=graphs;
    adapter=new ArrayAdapter<String>(getActivity(),
      android.R.layout.simple_list_item_1, savedGraphs);
    setListAdapter(adapter);
    registerForContextMenu(getListView());
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view,
        int position, long id)
      {
        listener.onSelect(position);
      }
    });
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
    ContextMenuInfo menuInfo)
  {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.add(0, v.getId(), 0, R.string.saved_dialog_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
    adapter.remove(graphs.get(info.position));
    listener.onDelete(info.position);
    return super.onContextItemSelected(item);
  }
  
  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    listener=(OnItemSelectedListener)activity;
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
  {
    View view = inflater.inflate(R.layout.plain_list, container, false);
    return view;
  }
  
  public interface OnItemSelectedListener
  {
    public void onSelect(int position);
    public void onDelete(int position);
  }
}

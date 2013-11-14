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


import roboguice.fragment.RoboListFragment;
import roboguice.inject.ContentView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.inject.Inject;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.functions.AbsoluteFunction;
import com.tnc.android.graphite.functions.DerivativeFunction;
import com.tnc.android.graphite.functions.GraphFunction;
import com.tnc.android.graphite.functions.IntegralFunction;
import com.tnc.android.graphite.models.Target;
import com.tnc.android.graphite.utils.CurrentTargetList;


@ContentView(R.layout.plain_list)
public class EditGraphsFragment extends RoboListFragment
{
  @Inject CurrentTargetList list;
  @Inject Activity activity;
  @Inject Context context;
  
  private ArrayAdapter<Target> adapter;
  private ContextMenu contextMenu;
  private AdapterContextMenuInfo menuInfo;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    adapter=new ArrayAdapter<Target>(getActivity(),
      R.layout.edit_list_item, list)
    {
      public View getView(int position, View convertView, ViewGroup parent)
      {
        View view;
        LayoutInflater inflater=
          (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if(null==convertView)
        {
          view=inflater.inflate(R.layout.edit_list_item, parent, false);
        }
        else
        {
          view=convertView;
        }

        TextView text=(TextView)view.findViewById(R.id.edit_list_text);

        Target item=getItem(position);
        text.setText(item.getFullName());

        return view;
      }
    };
    setListAdapter(adapter);
  }
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    registerForContextMenu(getListView());
    
    // Open context menu with single click
    getListView().setOnItemClickListener(new OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view,
        int position, long id)
      {
        parent.showContextMenuForChild(view);
      }
    });
  }
  
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
    ContextMenuInfo menuInfo)
  {
    super.onCreateContextMenu(menu, v, menuInfo);
    contextMenu=menu;
    MenuInflater inflater=activity.getMenuInflater();
    inflater.inflate(R.menu.edit_context_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    AdapterContextMenuInfo info=(AdapterContextMenuInfo)item.getMenuInfo();
    if(null!=info)
    {
      menuInfo=info;
    }
    
    switch(item.getItemId())
    {
      case R.id.edit_context_menu_remove:
        list.remove(menuInfo.position);
        adapter.notifyDataSetChanged();
        list.setChanged(true);
        return true;
      // Fix for sub-sub-menus
      case R.id.edit_context_menu_function_transform_redirect:
        getListView().post(new Runnable()
        {
          public void run()
          {
            contextMenu.performIdentifierAction(
              R.id.edit_context_menu_function_transform, 0);                  
          }
        });
        return true;
      case R.id.edit_context_menu_function_derivative:
        applyFunction(new DerivativeFunction(), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_integral:
        applyFunction(new IntegralFunction(), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_absolute:
        applyFunction(new AbsoluteFunction(), menuInfo.position);
        return true;
      case R.id.edit_context_menu_undo:
        Target target=list.get(menuInfo.position);
        target.removeFunction();
        adapter.notifyDataSetChanged();
        list.setChanged(true);
        return true;
    }
    
    return super.onContextItemSelected(item);
  }
  
  private void applyFunction(GraphFunction function, int position)
  {
    Target target=list.get(position);
    target.addFunction(function);
    adapter.notifyDataSetChanged();
    list.setChanged(true);
  }
}

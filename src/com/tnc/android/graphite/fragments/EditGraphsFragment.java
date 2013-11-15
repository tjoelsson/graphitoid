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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.TextView;
import com.google.inject.Inject;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.functions.GraphFunction;
import com.tnc.android.graphite.functions.NoParameterFunction;
import com.tnc.android.graphite.functions.SingleParameterFunction;
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
      case R.id.edit_context_menu_function_calculate_redirect:
        getListView().post(new Runnable()
        {
          public void run()
          {
            contextMenu.performIdentifierAction(
              R.id.edit_context_menu_function_calculate, 0);                  
          }
        });
        return true;

      // Transform
      case R.id.edit_context_menu_function_scale:
        _singleParameterFunction("scale", menuInfo.position,
          R.string.function_instructions_scale, InputType.TYPE_NUMBER_FLAG_DECIMAL);
        return true;
      case R.id.edit_context_menu_function_derivative:
        _applyFunction(new NoParameterFunction("derivative"), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_integral:
        _applyFunction(new NoParameterFunction("integral"), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_absolute:
        _applyFunction(new NoParameterFunction("absolute"), menuInfo.position);
        return true;
      
      // Calculate
      case R.id.edit_context_menu_function_moving_average:
        _singleParameterFunction("movingAverage", menuInfo.position,
          R.string.function_instructions_moving_average, InputType.TYPE_CLASS_NUMBER);
        return true;
      case R.id.edit_context_menu_function_moving_median:
        _singleParameterFunction("movingMedian", menuInfo.position,
          R.string.function_instructions_moving_median, InputType.TYPE_CLASS_NUMBER);
        return true;
      case R.id.edit_context_menu_function_moving_standard_deviation:
        _singleParameterFunction("stdev", menuInfo.position,
          R.string.function_instructions_moving_standard_deviation, InputType.TYPE_CLASS_NUMBER);
        return true;
      case R.id.edit_context_menu_function_holtwinters_forecast:
        _applyFunction(new NoParameterFunction("holtWintersForecast"), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_holtwinters_confidence_bands:
        _applyFunction(new NoParameterFunction("holtWintersConfidenceBands"), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_holtwinters_aberration:
        _applyFunction(new NoParameterFunction("holtWintersAberration"), menuInfo.position);
        return true;
      case R.id.edit_context_menu_function_as_percent:
        _singleParameterFunction("asPercent", menuInfo.position,
          R.string.function_instructions_as_percent, InputType.TYPE_NUMBER_FLAG_DECIMAL, true);
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
  
  private void _singleParameterFunction(String functionName, int position,
    int title, int type)
  {
    _singleParameterFunction(functionName, position, title, type, false);
  }

  private void _singleParameterFunction(String functionName, int position,
    int title, int type, boolean allowEmpty)
  {
    final String fFunctionName=functionName;
    final int fPosition=position;
    _showInputDialog(title, type, new FunctionInputCallback()
    {
      @Override
      public void call(String param)
      {
        _applyFunction(new SingleParameterFunction(fFunctionName, param),
          fPosition);
      }
    }, allowEmpty);
  }
  
  private void _showInputDialog(int title, int type,
    FunctionInputCallback callback, boolean allowEmpty)
  {
    final FunctionInputCallback fCallback=callback;
    final boolean fAllowEmpty=allowEmpty;
    final EditText input=new EditText(activity);
    input.setInputType(type);

    AlertDialog.Builder builder=new AlertDialog.Builder(activity);
    builder.setTitle(title);
    builder.setView(input);

    builder.setPositiveButton(R.string.dialog_positive,
      new DialogInterface.OnClickListener()
    { 
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        if(fAllowEmpty||0<input.length())
        {
          fCallback.call(input.getText().toString());
        }
      }
    });
    builder.setNegativeButton(R.string.dialog_negative,
      new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        dialog.cancel();
      }
    });

    builder.show();
  }
  
  private void _applyFunction(GraphFunction function, int position)
  {
    Target target=list.get(position);
    target.addFunction(function);
    adapter.notifyDataSetChanged();
    list.setChanged(true);
  }
  
  interface FunctionInputCallback
  {
    public void call(String input);
  }
}

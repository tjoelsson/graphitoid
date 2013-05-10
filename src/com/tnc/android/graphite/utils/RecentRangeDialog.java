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


import java.util.Arrays;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.activities.TargetsActivity;


public class RecentRangeDialog extends Dialog
  implements android.view.View.OnClickListener
{
  private Activity activity;
  private EditText numberField;
  private Spinner unitSpinner;
  private Integer number;
  private String unit;
    
  public RecentRangeDialog(Activity activity)
  {
    super(activity);
    this.activity = activity;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    // Set title
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.recent_range_dialog);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.dialog_title);
    TextView titleText=(TextView)findViewById(R.id.dialog_title_text);
    titleText.setText(R.string.recent_range_dialog_title);
    
    // Number field
    numberField=(EditText)findViewById(R.id.time_input);
    if(null!=number)
    {
      numberField.setText(String.valueOf(number));
    }
    
    // Unit spinner
    unitSpinner=(Spinner)findViewById(R.id.units_spinner);
    final ArrayAdapter<String> adapter=new ArrayAdapter<String>(activity,
      android.R.layout.simple_spinner_item,
      activity.getResources().getStringArray(R.array.time_range_units));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    unitSpinner.setAdapter(adapter);
    if(null==unit)
    {
      unitSpinner.setSelection(1);
    }
    else
    {
      CharSequence[] units=activity.getResources().getTextArray(R.array.time_range_units);
      int index=Arrays.asList(units).indexOf(unit);
      unitSpinner.setSelection(index);
    }
    
    // Buttons
    Button ok=(Button)findViewById(R.id.recent_range_dialog_ok);
    Button cancel=(Button)findViewById(R.id.recent_range_dialog_cancel);
    ok.setOnClickListener(this);
    cancel.setOnClickListener(this);
  }

  @Override
  public void onClick(View v)
  {
    switch(v.getId())
    {
      case R.id.recent_range_dialog_ok:
        ((TargetsActivity)activity).timeRangeDialogCallback(
          Integer.valueOf(numberField.getText().toString()),
          (String)unitSpinner.getSelectedItem());
        break;
      case R.id.recent_range_dialog_cancel:
        break;
    }
    dismiss();
  }
  
  public void setNumber(Integer number)
  {
    this.number=number;
  }
  
  public void setUnit(String unit)
  {
    this.unit=unit;
  }
}

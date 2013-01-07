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

package com.tnc.android.graphite.fragments;


import java.util.Calendar;
import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.*;
import android.os.Bundle;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.tnc.android.graphite.activities.TargetsActivity;
import com.tnc.android.graphite.utils.IntervalPickerSlider;


public class IntervalDialogFragment extends DialogFragment
{

  private Calendar cal=null;
  private int type;
  private Activity activity;
  private String header;

  public IntervalDialogFragment(Activity activity, Calendar cal, int type,
    String header)
  {
    super();
    this.activity=activity;
    this.cal=cal;
    this.type=type;
    this.header=header;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    return new IntervalPickerSlider(activity, mTimeListener, cal, header);
  }

  private DateSlider.OnDateSetListener mTimeListener=new DateSlider.OnDateSetListener() {
    public void onDateSet(DateSlider view, Calendar selectedDate)
    {
      ((TargetsActivity)activity).intervalDialogCallback(selectedDate, type);
    }
  };
}

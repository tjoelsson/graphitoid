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


import java.util.Calendar;
import android.content.Context;
import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.R;


public class IntervalPickerSlider extends DateSlider
{
  private String header;

  public IntervalPickerSlider(Context context, OnDateSetListener l,
    Calendar calendar, String header)
  {
    super(context, R.layout.datetimeslider, l, calendar, null, null);
    this.header=header;
  }

  @Override
  protected void setTitle()
  {
    if(null!=mTitleText)
    {
      mTitleText.setText(header);
    }
  }
}

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

package com.tnc.android.graphite.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;


public class SwipeView extends ScrollView
{
  GestureDetector gestureDetector;

  public SwipeView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public void setGestureDetector(GestureDetector gestureDetector)
  {
    this.gestureDetector=gestureDetector;
  }
  
  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev)
  {
    if(gestureDetector.onTouchEvent(ev))
    {
      return true;
    }
    
    return super.onInterceptTouchEvent(ev);
  }
}

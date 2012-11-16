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


import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import com.tnc.android.graphite.activities.BaseActivity;


public class SwipeGestureListener extends SimpleOnGestureListener
{
  private static final int SWIPE_MIN_DISTANCE=120;
  private static final int SWIPE_MAX_OFF_PATH=200;
  private static final int SWIPE_THRESHOLD_VELOCITY=200;

  private BaseActivity activity;

  public SwipeGestureListener(BaseActivity a)
  {
    super();
    activity=a;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
    float velocityY)
  {
    try
    {
      float diffAbs=Math.abs(e1.getY()-e2.getY());
      float diff=e1.getX()-e2.getX();

      if(diffAbs>SWIPE_MAX_OFF_PATH)
        return false;

      // Left swipe
      if(diff>SWIPE_MIN_DISTANCE
        &&Math.abs(velocityX)>SWIPE_THRESHOLD_VELOCITY)
      {
        activity.onLeftSwipe();

        // Right swipe
      }
      else if(-diff>SWIPE_MIN_DISTANCE
        &&Math.abs(velocityX)>SWIPE_THRESHOLD_VELOCITY)
      {
        activity.onRightSwipe();
      }
    }
    catch(Exception e)
    {
      Log.e("YourActivity", "Error on gestures");
    }
    return false;
  }

  // It is necessary to return true from onDown for the onFling event to register
  @Override
  public boolean onDown(MotionEvent e)
  {
    return true;
  }
}

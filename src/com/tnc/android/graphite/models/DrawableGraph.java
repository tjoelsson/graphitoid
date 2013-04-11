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

package com.tnc.android.graphite.models;


import android.graphics.drawable.Drawable;


public class DrawableGraph extends SimpleObservable<DrawableGraph>
{
  private Drawable image;

  public Drawable getImage()
  {
    return this.image;
  }

  public void setImage(Drawable image)
  {
    this.image=image;
  }
 
  @Override
  synchronized public DrawableGraph clone()
  {
    DrawableGraph dg=new DrawableGraph();
    dg.setImage(this.image);
    return dg;
  }

  synchronized public void consume(DrawableGraph dg)
  {
    this.image=dg.getImage();
    notifyObservers(this);
  }
}

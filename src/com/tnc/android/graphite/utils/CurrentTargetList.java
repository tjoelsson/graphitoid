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

package com.tnc.android.graphite.utils;


import java.util.ArrayList;
import com.google.inject.Singleton;
import com.tnc.android.graphite.models.Target;


@Singleton
public class CurrentTargetList extends ArrayList<Target>
{
  private static final long serialVersionUID=-1893253020156312567L;
  private boolean changed=false;
  
  public boolean isChanged()
  {
    return changed;
  }
  public void setChanged(boolean changed)
  {
    this.changed=changed;
  }
}

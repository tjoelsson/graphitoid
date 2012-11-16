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

package com.tnc.android.graphite.controllers;


import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Message;


abstract class Controller
{
  @SuppressWarnings("unused")
  private static final String TAG=Controller.class.getSimpleName();
  private final List<Handler> outboxHandlers=new ArrayList<Handler>();

  public Controller()
  {
  }

  public void dispose()
  {
  }

  abstract public boolean handleMessage(int what, Object data);

  public boolean handleMessage(int what)
  {
    return handleMessage(what, null);
  }

  public final void addOutboxHandler(Handler handler)
  {
    outboxHandlers.add(handler);
  }

  public final void removeOutboxHandler(Handler handler)
  {
    outboxHandlers.remove(handler);
  }

  protected final void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj)
  {
    if(!outboxHandlers.isEmpty())
    {
      for(Handler handler : outboxHandlers)
      {
        Message msg=Message.obtain(handler, what, arg1, arg2, obj);
        msg.sendToTarget();
      }
    }
  }
}

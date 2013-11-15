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

package com.tnc.android.graphite.functions;


import java.io.Serializable;


public class SingleParameterFunction implements GraphFunction, Serializable
{
  private String functionName;
  private String param;
  
  public SingleParameterFunction(String functionName, String param)
  {
    this.functionName=functionName;
    this.param=param;
  }
  
  @Override
  public String apply(String target)
  {
    String retVal;
    if(0==param.length())
    {
      // Some functions allow the parameter to be empty
      retVal=functionName+"("+target+")";
    }
    else
    {
      retVal=functionName+"("+target+","+param+")";
    }
    return retVal;
  }
}

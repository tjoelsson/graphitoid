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

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;

public class RecentRange implements Parcelable, Serializable
{
  private static final long serialVersionUID=-6698588001469856414L;
  private Integer value;
  private String unit;
  
  public RecentRange(Integer value, String unit)
  {
    this.value=value;
    this.unit=unit;
  }
  
  public RecentRange(Parcel in)
  {
    readFromParcel(in);
  }
  
  public Integer getValue()
  {
    return value;
  }
  
  public void setValue(Integer value)
  {
    this.value=value;
  }
  
  public String getUnit()
  {
    return unit;
  }
  
  public void setUnit(String unit)
  {
    this.unit=unit;
  }

  @Override
  public int describeContents()
  {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags)
  {
    out.writeInt(value);
    out.writeString(unit);
  }
  
  private void readFromParcel(Parcel in) {
    value=in.readInt();
    unit=in.readString();
  }
  
  public static final Parcelable.Creator CREATOR=new Parcelable.Creator()
  {
    public RecentRange createFromParcel(Parcel in)
    {
      return new RecentRange(in);
    }
    public RecentRange[] newArray(int size)
    {
      return new RecentRange[size];
    }
  };
}

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

package com.tnc.android.graphite.daos;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tnc.android.graphite.GraphiteApp;


final class DatabaseHelper extends SQLiteOpenHelper
{
  @SuppressWarnings("unused")
  private static final String TAG=DatabaseHelper.class.getSimpleName();
  private static final String DATABASE_NAME="graphitoid";
  private static final int DATABASE_VERSION=1;

  public DatabaseHelper()
  {
    super(GraphiteApp.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database)
  {
    final String counter="CREATE TABLE "+TargetDao.TABLE+"("+
      TargetDao._ID+" integer primary key, "+
      TargetDao.HASH+" text, "+
      TargetDao.NAME+" text, "+
      TargetDao.ENABLED+" int, "+
      TargetDao.EXPANDABLE+" int)";
    database.execSQL(counter);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    // first iteration. do nothing.
  }
}

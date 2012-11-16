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


import java.util.ArrayList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.tnc.android.graphite.models.Target;


public class TargetDao
{
  protected static final String TABLE="Target";
  protected static final String _ID="_id";
  protected static final String HASH="hash";
  protected static final String NAME="name";
  protected static final String ENABLED="enabled";
  protected static final String EXPANDABLE="expandable";

  public TargetDao()
  {
  }

  public ArrayList<Target> getAll()
  {
    ArrayList<Target> list=new ArrayList<Target>();
    SQLiteDatabase db=new DatabaseHelper().getWritableDatabase();
    Cursor cursor=db.query(TABLE, null, null, null, null, null, null);
    while(cursor.moveToNext())
    {
      Target t=new Target();
      t.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
      t.setHash(cursor.getString(cursor.getColumnIndex(HASH)));
      t.setName(cursor.getString(cursor.getColumnIndex(NAME)));
      t.setEnabled(cursor.getInt(cursor.getColumnIndex(ENABLED))==1);
      t.setExpandable(cursor.getInt(cursor.getColumnIndex(EXPANDABLE))==1);
      list.add(t);
    }

    cursor.close();
    db.close();
    return list;
  }

  public Target get(int id)
  {
    SQLiteDatabase db=new DatabaseHelper().getWritableDatabase();
    Cursor cursor=db.query(TABLE, null, _ID+"=?", new String[] {
      Integer.toString(id)}, null, null, null);
    Target t=null;
    if(cursor.moveToFirst())
    {
      t=new Target();
      t.setId(cursor.getInt(cursor.getColumnIndex(_ID)));
      t.setHash(cursor.getString(cursor.getColumnIndex(HASH)));
      t.setName(cursor.getString(cursor.getColumnIndex(NAME)));
      t.setEnabled(cursor.getInt(cursor.getColumnIndex(ENABLED))==1);
      t.setExpandable(cursor.getInt(cursor.getColumnIndex(EXPANDABLE))==1);
    }

    cursor.close();
    db.close();
    return t;
  }

  public long insert(Target target)
  {
    SQLiteDatabase db=new DatabaseHelper().getWritableDatabase();
    ContentValues values=new ContentValues();
    if(target.getId()>0)
      values.put(_ID, target.getId());
    values.put(HASH, target.getHash());
    values.put(NAME, target.getName());
    values.put(ENABLED, target.isEnabled());
    values.put(EXPANDABLE, target.isExpandable());

    long num=db.insert(TABLE, null, values);
    db.close();
    return num;
  }

  public int update(Target target)
  {
    SQLiteDatabase db=new DatabaseHelper().getWritableDatabase();
    ContentValues values=new ContentValues();
    values.put(_ID, target.getId());
    values.put(HASH, target.getHash());
    values.put(NAME, target.getName());
    values.put(ENABLED, target.isEnabled());
    values.put(EXPANDABLE, target.isExpandable());

    int num=db.update(TABLE, values, _ID+"=?", new String[] {
      Integer.toString(target.getId())});
    db.close();
    return num;
  }

  public void delete(int id)
  {
    SQLiteDatabase db=new DatabaseHelper().getWritableDatabase();
    db.delete(TABLE, _ID+"=?", new String[] {Integer.toString(id)});
    db.close();
  }

  public void delete(Target target)
  {
    delete(target.getId());
  }

  public void deleteAll()
  {
    SQLiteDatabase db=new DatabaseHelper().getWritableDatabase();
    db.delete(TABLE, null, null);
    db.close();
  }
}

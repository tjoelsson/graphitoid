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


import java.util.List;


public class Graph extends SimpleObservable<Graph>
{
  private List<List<Double>> values;
  private List<String> names;
  private List<Integer> steps;
  private int from;
  private int to;

  public List<List<Double>> getValues()
  {
    return values;
  }

  public void setValues(List<List<Double>> values)
  {
    this.values=values;
  }

  public List<String> getNames()
  {
    return names;
  }

  public void setNames(List<String> names)
  {
    this.names=names;
  }

  public List<Integer> getSteps()
  {
    return steps;
  }

  public void setSteps(List<Integer> steps)
  {
    this.steps=steps;
  }

  public int getFrom()
  {
    return from;
  }

  public void setFrom(int from)
  {
    this.from=from;
  }

  public int getTo()
  {
    return to;
  }

  public void setTo(int to)
  {
    this.to=to;
  }

  @Override
  synchronized public Graph clone()
  {
    Graph g=new Graph();
    g.setValues(values);
    g.setNames(names);
    g.setSteps(steps);
    g.setFrom(from);
    g.setTo(to);
    return g;
  }

  synchronized public void consume(Graph g)
  {
    this.values=g.getValues();
    this.names=g.getNames();
    this.steps=g.getSteps();
    this.from=g.getFrom();
    this.to=g.getTo();
    notifyObservers(this);
  }
}

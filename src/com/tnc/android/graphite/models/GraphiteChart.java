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


import java.util.Date;
import java.util.List;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.content.Context;
import android.graphics.Color;
import android.view.View;


public class GraphiteChart
{
  final private int[] COLORS= {
    Color.parseColor("#65E657"), // Green
    Color.parseColor("#6EAFF5"), // Blue
    Color.parseColor("#E34F4F"), // Red
    Color.parseColor("#D04FDB"), // Purple
    Color.parseColor("#805C37"), // Brown
    Color.parseColor("#E3E36F"), // Yellow
    Color.RED, Color.CYAN, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.WHITE};

  final private int DAY=60*60*24;

  public View createView(Context context, Graph graph)
  {
    // Create renderer
    XYMultipleSeriesRenderer renderer=new XYMultipleSeriesRenderer();
    renderer.setAxisTitleTextSize(14);
    renderer.setChartTitleTextSize(20);
    renderer.setLabelsTextSize(12);
    renderer.setLegendTextSize(16);

    for(int i=0; i<graph.getValues().size(); i++)
    {
      XYSeriesRenderer r=new XYSeriesRenderer();
      r.setColor(COLORS[i%COLORS.length]);
      r.setPointStyle(PointStyle.POINT);
      renderer.addSeriesRenderer(r);
    }

    renderer.setYLabels(8);
    renderer.setShowGrid(true);
    renderer.setZoomEnabled(false, false);
    renderer.setPanEnabled(false, false);
    renderer.setFitLegend(true);

    // Create dataset
    XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
    for(int i=0; i<graph.getValues().size(); i++)
    {
      TimeSeries series=new TimeSeries(graph.getNames().get(i));
      List<Double> floatValues=graph.getValues().get(i);

      for(int k=0; k<floatValues.size(); k++)
      {
        Date date=new Date((long)(graph.getFrom()+(graph.getSteps().get(i).intValue()*k))*1000);
        series.add(date, floatValues.get(k));
      }

      dataset.addSeries(series);
    }

    if(dataset.getSeriesCount()!=renderer.getSeriesRendererCount())
    {
      throw new IllegalArgumentException(
        "Dataset and renderer should have the same number of series");
    }

    TimeChart chart=new TimeChart(dataset, renderer);

    int diff=graph.getTo()-graph.getFrom();
    String format="MMM";
    if(120*DAY>=diff)
    {
      format="'Week' ww";
    }
    if(14*DAY>=diff)
    {
      format="EEE";
    }
    if(5*DAY>=diff)
    {
      format="EEE ha";
    }
    if(DAY>=diff)
    {
      format="HH':'mm";
    }

    chart.setDateFormat(format);
    chart.setXAxisSmart(true);
    return new GraphicalView(context, chart);
  }
}

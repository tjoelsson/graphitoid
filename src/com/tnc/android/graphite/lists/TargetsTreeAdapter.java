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

package com.tnc.android.graphite.lists;


import java.util.List;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.tnc.android.graphite.R;
import com.tnc.android.graphite.activities.TargetsActivity;
import com.tnc.android.graphite.models.Target;


public class TargetsTreeAdapter extends AbstractTreeViewAdapter<Target>
{
  private final List<Target> selected;

  private final OnCheckedChangeListener onCheckedChange=
    new OnCheckedChangeListener()
    {
      @Override
      public void onCheckedChanged(final CompoundButton buttonView,
        final boolean isChecked)
      {
        final Target id=(Target)buttonView.getTag();
        changeSelected(isChecked, id);
      }
    };

  private void changeSelected(final boolean isChecked, final Target id)
  {
    if(isChecked)
    {
      if(!selected.contains(id))
      {
        selected.add(id);
      }
    }
    else
    {
      selected.remove(id);
    }
  }

  public TargetsTreeAdapter(final Activity activity,
    final List<Target> selected,
    final TreeStateManager<Target> treeStateManager,
    final int numberOfLevels)
  {
    super(activity, treeStateManager, numberOfLevels);
    this.selected=selected;
  }

  private String getDescription(final Target id)
  {
    String name=id.getName();
    int idx=name.lastIndexOf('.');
    if(0<=idx)
    {
      name=name.substring(idx+1, name.length());
    }
    return name;
  }

  @Override
  public View getNewChildView(final TreeNodeInfo<Target> treeNodeInfo)
  {
    final LinearLayout viewLayout=(LinearLayout)getActivity()
      .getLayoutInflater().inflate(R.layout.target_list_item, null);
    return updateView(viewLayout, treeNodeInfo);
  }

  @Override
  public LinearLayout updateView(final View view,
    final TreeNodeInfo<Target> treeNodeInfo)
  {
    final LinearLayout viewLayout=(LinearLayout)view;
    final TextView descriptionView=(TextView)viewLayout
      .findViewById(R.id.list_item_description);
    descriptionView.setText(getDescription(treeNodeInfo.getId()));
    final CheckBox box=(CheckBox)viewLayout
      .findViewById(R.id.list_checkbox);
    Target target=treeNodeInfo.getId();
    viewLayout.setVisibility(View.VISIBLE);
    if(target.isPlaceholder())
    {
      viewLayout.setVisibility(View.GONE);
    }
    else if(target.isExpandable())
    {
      box.setVisibility(View.GONE);
    }
    else
    {
      box.setTag(target); // has to be set before calling setChecked
      box.setVisibility(View.VISIBLE);
      box.setChecked(selected.contains(target));
      box.setOnCheckedChangeListener(onCheckedChange);
    }
    return viewLayout;
  }

  @Override
  public void handleItemClick(final View view, final Object id)
  {
    final Target longId=(Target)id;
    final TreeNodeInfo<Target> info=getManager().getNodeInfo(longId);
    if(info.isWithChildren())
    {
      super.handleItemClick(view, id);
    }
    else
    {
      final ViewGroup vg=(ViewGroup)view;
      final CheckBox cb=(CheckBox)vg
        .findViewById(R.id.list_checkbox);
      cb.performClick();
    }
  }

  @Override
  public long getItemId(final int position)
  {
    //        return getTreeId(position);
    return 0;
  }

  @Override
  protected void expandCollapse(final Target target)
  {
    final TreeNodeInfo<Target> info=this.getManager().getNodeInfo(target);
    if(!info.isExpanded()&&!info.getId().isLoaded())
    {
      ((TargetsActivity)getActivity()).getMoreTargets(target);
    }
    super.expandCollapse(target);
  }
}

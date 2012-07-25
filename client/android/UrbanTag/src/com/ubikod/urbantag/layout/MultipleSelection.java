/*
 * Copyright 2012 Ubikod
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.ubikod.urbantag.layout;

import java.util.ArrayList;
import java.util.List;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MultipleSelection<T> extends Spinner implements OnMultiChoiceClickListener,
  OnCancelListener
{

  /**
   * List containing all items to display
   */
  private List<T> items;

  /**
   * Boolean array which marks which item are selected or not
   */
  private boolean[] selected;

  /**
   * Text to display on spinner
   */
  private String defaultText;

  /**
   * Listener we have to notify when validating choices
   */
  private MultiSpinnerListener<T> listener;

  /**
   * Constructor
   * @param context
   */
  public MultipleSelection(Context context)
  {
    super(context);
  }

  /**
   * Constructor
   * @param arg0
   * @param arg1
   */
  public MultipleSelection(Context arg0, AttributeSet arg1)
  {
    super(arg0, arg1);
  }

  /**
   * Constructor
   * @param arg0
   * @param arg1
   * @param arg2
   */
  public MultipleSelection(Context arg0, AttributeSet arg1, int arg2)
  {
    super(arg0, arg1, arg2);
  }

  @Override
  /**
   * Method reacting when clicking on item. Toggle selection for clicked item.
   */
  public void onClick(DialogInterface dialog, int which, boolean isChecked)
  {
    if (isChecked)
      selected[which] = true;
    else
      selected[which] = false;
  }

  @Override
  /**
   * Action to perform when clicking back button
   */
  public void onCancel(DialogInterface dialog)
  {
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
      android.R.layout.simple_spinner_item, new String[] { defaultText });
    setAdapter(adapter);
    List<T> selectedItems = new ArrayList<T>();
    for (int i = 0; i < selected.length; i++)
    {
      if (selected[i])
      {
        selectedItems.add(items.get(i));
      }
    }
    listener.onItemsSelected(selectedItems);
  }

  @Override
  /**
   * Action to perform when clicking on spinner
   */
  public boolean performClick()
  {
    String[] itemsNames = new String[items.size()];
    for (int i = 0; i < itemsNames.length; i++)
    {
      itemsNames[i] = items.get(i).toString();
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMultiChoiceItems(itemsNames, selected, this);
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        dialog.cancel();
      }
    });
    builder.setOnCancelListener(this);
    builder.show();
    return true;
  }

  /**
   * Set elements for view
   * @param items List of items to display
   * @param selectedItems Mark which item are selected. If array's length is lower than items list
   *          size missing elements will be marked as unselected.
   * @param allText Text to display on spinner
   * @param listener Listener we notify after closing dialog box
   */
  public void setItems(List<T> items, boolean[] selectedItems, String allText,
    MultiSpinnerListener<T> listener)
  {
    this.items = items;
    this.defaultText = allText;
    this.listener = listener;

    selected = new boolean[items.size()];
    for (int i = 0; i < selected.length && i < selectedItems.length; i++)
      selected[i] = selectedItems[i];

    // all text on the spinner
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
      android.R.layout.simple_spinner_item, new String[] { defaultText });
    setAdapter(adapter);
  }

  /**
   * Interface for listeners
   * @author cdesneuf
   * @param <T> Type of objects which will be passed to dialog box. They need to implement a valid
   *          toString method !
   */
  public interface MultiSpinnerListener<T>
  {
    public void onItemsSelected(List<T> selectedItems);
  }
}

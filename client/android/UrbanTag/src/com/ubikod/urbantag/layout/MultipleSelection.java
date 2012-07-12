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

  private List<T> items;
  private boolean[] selected;
  private String defaultText;
  private MultiSpinnerListener<T> listener;

  public MultipleSelection(Context context)
  {
    super(context);
  }

  public MultipleSelection(Context arg0, AttributeSet arg1)
  {
    super(arg0, arg1);
  }

  public MultipleSelection(Context arg0, AttributeSet arg1, int arg2)
  {
    super(arg0, arg1, arg2);
  }

  @Override
  public void onClick(DialogInterface dialog, int which, boolean isChecked)
  {
    if (isChecked)
      selected[which] = true;
    else
      selected[which] = false;
  }

  @Override
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

  public interface MultiSpinnerListener<T>
  {
    public void onItemsSelected(List<T> selectedItems);
  }
}
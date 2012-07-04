package com.ubikod.urbantag;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PlaceOverlayItem extends OverlayItem
{
  private Drawable marker;

  public PlaceOverlayItem(GeoPoint point, String title, String text, int color)
  {
    super(point, title, text);
    marker = new TextDrawable(title, color);
    super.setMarker(marker);
  }

  public Drawable getMarker(int stateBitset)
  {
    return this.marker;
  }
}

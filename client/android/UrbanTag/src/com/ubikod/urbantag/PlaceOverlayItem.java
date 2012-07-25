package com.ubikod.urbantag;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Overlay item for places
 * @author cdesneuf
 */
public class PlaceOverlayItem extends OverlayItem
{
  private Drawable marker;

  /**
   * Constructor
   * @param point position
   * @param title Place name
   * @param text
   * @param color background color
   */
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

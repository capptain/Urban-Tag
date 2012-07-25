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

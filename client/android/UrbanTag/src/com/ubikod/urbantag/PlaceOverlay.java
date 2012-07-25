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

import java.util.ArrayList;
import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.ubikod.urbantag.model.Place;

/**
 * Class for a places map overlay
 * @author cdesneuf
 */
public class PlaceOverlay extends ItemizedOverlay<OverlayItem>
{
  private Vector<Place> items = new Vector<Place>();
  private UrbanTagMainActivity context;

  /**
   * Constructor
   * @param context
   * @param defaultMarker
   */
  public PlaceOverlay(UrbanTagMainActivity context, Drawable defaultMarker)
  {
    super(boundCenterBottom(defaultMarker));
    this.context = context;
  }

  /**
   * Empty overlay
   */
  public void clear()
  {
    items.clear();
    populate();
  }

  /**
   * Add several places to overlay
   * @param places
   */
  public void setPlaces(Vector<Place> places)
  {
    this.items = places;
    populate();
  }

  /**
   * Add a place to overlay
   * @param p
   */
  public void addPlace(Place p)
  {
    int index = -1;
    if ((index = items.indexOf(p)) == -1)
    {
      items.add(p);
    }
    else
    {
      items.remove(index);
      items.add(index, p);
    }
    populate();
  }

  @Override
  protected OverlayItem createItem(int i)
  {
    return items.get(i).getOverlayItem();
  }

  @Override
  public int size()
  {
    return items.size();
  }

  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow)
  {
    super.draw(canvas, mapView, false);
  }

  @Override
  public boolean onTap(GeoPoint geoPoint, MapView mapView)
  {
    ArrayList<Integer> touchedPlace = new ArrayList<Integer>();
    boolean res = false;

    final Projection pj = mapView.getProjection();
    Point p = new Point(0, 0);
    Point t = new Point(0, 0);

    pj.toPixels(geoPoint, p);

    for (Place place : items)
    {
      PlaceOverlayItem item = place.getOverlayItem();
      pj.toPixels(item.getPoint(), t);

      if (hitTest(item, item.getMarker(0), p.x - t.x, p.y - t.y))
      {
        touchedPlace.add(place.getId());
        res = true;
      }
    }
    // Send place to map view
    context.onMapTap(touchedPlace);

    return res;
  }
}

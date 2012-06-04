package com.ubikod.urbantag;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class PlaceOverlay extends ItemizedOverlay<OverlayItem>
{
  private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
  private Context mContext;
  private Drawable marker;

  public PlaceOverlay(Context context, Drawable defaultMarker)
  {
    super(boundCenterBottom(defaultMarker));
    mContext = context;
    this.marker = defaultMarker;
  }

  public void addOverlay(OverlayItem overlay)
  {
    mOverlays.add(overlay);
    populate();
  }

  @Override
  protected OverlayItem createItem(int i)
  {
    return mOverlays.get(i);
  }

  @Override
  public int size()
  {
    return mOverlays.size();
  }

  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow)
  {
    super.draw(canvas, mapView, false);
  }

  // @Override
  public boolean onTap(GeoPoint geoPoint, MapView mapView)
  {
    boolean res = false;

    final Projection pj = mapView.getProjection();
    Point p = new Point(0, 0);
    Point t = new Point(0, 0);

    pj.toPixels(geoPoint, p);

    for (OverlayItem item : mOverlays)
    {
      pj.toPixels(item.getPoint(), t);
      if (hitTest(item, new TextDrawable(item.getTitle()), p.x - t.x, p.y - t.y))
      {
        Log.i("place", item.getTitle());
        res = true;
      }
    }

    return res;
  }
  // @Override
  // protected boolean onTap(int index)
  // {
  // OverlayItem item = mOverlays.get(index);
  // AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
  // dialog.setTitle(item.getTitle());
  // dialog.setMessage(item.getSnippet());
  // dialog.show();
  // return true;
  // }

}

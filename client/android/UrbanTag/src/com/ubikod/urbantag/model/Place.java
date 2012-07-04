package com.ubikod.urbantag.model;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.ubikod.urbantag.PlaceOverlayItem;

public class Place
{
  private int id;
  private String name;
  private Tag mainTag;
  private GeoPoint pos;
  private List<Tag> allTags;
  private PlaceOverlayItem overlayItem;

  public Place(int id, String name, Tag mainTag, GeoPoint position, List<Tag> allTags)
  {
    this.id = id;
    this.name = name;
    this.mainTag = mainTag;
    this.pos = position;
    this.allTags = allTags;
    this.overlayItem = new PlaceOverlayItem(this.pos, this.name, this.mainTag.getValue(),
      this.mainTag.getColor());

  }

  public String getName()
  {
    return this.name;
  }

  public int getId()
  {
    return this.id;
  }

  public Tag getMainTag()
  {
    return this.mainTag;
  }

  public List<Tag> getAllTags()
  {
    return this.allTags;
  }

  public GeoPoint getPosition()
  {
    return this.pos;
  }

  public int getColor()
  {
    return this.mainTag.getColor();
  }

  public PlaceOverlayItem getOverlayItem()
  {
    return this.overlayItem;
  }

  public boolean equals(Object o)
  {
    if (o instanceof Place)
    {
      Place p = (Place) o;
      return this.id == p.id;
    }
    return false;
  }
}

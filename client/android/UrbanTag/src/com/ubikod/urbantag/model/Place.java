package com.ubikod.urbantag.model;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.ubikod.urbantag.PlaceOverlayItem;

public class Place
{
  private int mId;
  private String mName;
  private Tag mMainTag;
  private GeoPoint mPos;
  private List<Tag> mAllTags;
  private PlaceOverlayItem mOverlayItem;

  public Place(int id, String name, Tag mainTag, GeoPoint position, List<Tag> allTags)
  {
    this.mId = id;
    this.mName = name;
    this.mMainTag = mainTag;
    this.mPos = position;
    this.mAllTags = allTags;
    this.mOverlayItem = new PlaceOverlayItem(this.mPos, this.mName, this.mMainTag.getValue(),
      this.mMainTag.getColor());

  }

  public String getName()
  {
    return this.mName;
  }

  public int getId()
  {
    return this.mId;
  }

  public Tag getMainTag()
  {
    return this.mMainTag;
  }

  public List<Tag> getAllTags()
  {
    return this.mAllTags;
  }

  public GeoPoint getPosition()
  {
    return this.mPos;
  }

  public int getColor()
  {
    return this.mMainTag.getColor();
  }

  public PlaceOverlayItem getOverlayItem()
  {
    return this.mOverlayItem;
  }

  public boolean equals(Object o)
  {
    if (o instanceof Place)
    {
      Place p = (Place) o;
      return this.mId == p.mId;
    }
    return false;
  }

  public boolean hasChanged(Object o)
  {
    if (o instanceof Place)
    {
      Place p = (Place) o;

      return !this.mName.equals(p.mName) || !this.mPos.equals(p.mPos)
        || !this.mMainTag.equals(p.mMainTag) || !this.mAllTags.equals(p.mAllTags);
    }
    else
      return false;
  }

}

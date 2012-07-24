package com.ubikod.urbantag.model;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.ubikod.urbantag.PlaceOverlayItem;

public class Place
{
  /** Place id */
  private int mId;

  /** Place name */
  private String mName;

  /** Place main tag */
  private Tag mMainTag;

  /** Place position */
  private GeoPoint mPos;

  /** Place tags */
  private List<Tag> mAllTags;

  /** Place associated overlay item */
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
      /* Place equality is defined by id equality */
      return this.mId == p.mId;
    }
    return false;
  }

  public boolean hasChanged(Object o)
  {
    if (o instanceof Place)
    {
      Place p = (Place) o;
      /* A place has changed if one of its attributes(except id) has changed */
      return !this.mName.equals(p.mName) || !this.mPos.equals(p.mPos)
        || !this.mMainTag.equals(p.mMainTag) || !this.mAllTags.equals(p.mAllTags);
    }
    else
      return false;
  }

}

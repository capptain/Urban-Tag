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

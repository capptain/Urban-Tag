package com.ubikod.urbantag.model;

import java.util.List;

public class Content
{
  /** Content id */
  private int mId;

  /** Content name */
  private String mName;

  /** Content start notification date */
  private int mStartDate;

  /** Content end notfication date */
  private int mEndDate;

  /** Content's place */
  private Place mPlace;

  /** Content main tag */
  private Tag mMainTag;

  /** Content tags */
  private List<Tag> mAllTags = null;

  public Content(int id, String name, int startDate, int endDate, Place place, Tag mainTag,
    List<Tag> allTags)
  {
    this.mId = id;
    this.mName = name;
    this.mStartDate = startDate;
    this.mEndDate = endDate;
    this.mPlace = place;
    this.mAllTags = allTags;
    this.mMainTag = mainTag;
  }

  public int getId()
  {
    return this.mId;
  }

  public String getName()
  {
    return this.mName;
  }

  public int getStartDate()
  {
    return this.mStartDate;
  }

  public int getEndDate()
  {
    return this.mEndDate;
  }

  public Place getPlace()
  {
    return this.mPlace;
  }

  public List<Tag> getAllTags()
  {
    return this.mAllTags;
  }

  public Tag getTag()
  {
    return this.mMainTag;
  }

  public boolean equals(Object o)
  {
    if (o instanceof Content)
    {
      Content c = (Content) o;
      /* Equality is defined by an equality of id */
      return this.mId == c.mId;
    }
    return false;
  }

  public boolean hasChanged(Object o)
  {
    if (o instanceof Content)
    {
      Content c = (Content) o;
      /* A content has changed if one its attributes (except id) has changed */
      return !this.mName.equals(c.mName) || this.mStartDate != c.mStartDate
        || this.mEndDate != c.mEndDate || !this.mMainTag.equals(c.mMainTag)
        || !this.mAllTags.equals(c.mAllTags) || !this.mPlace.equals(c.mPlace);
    }
    else
      return false;
  }
}

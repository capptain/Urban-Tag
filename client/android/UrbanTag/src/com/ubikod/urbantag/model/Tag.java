package com.ubikod.urbantag.model;

public class Tag
{
  /** Tag id */
  private int mId;

  /** Tag value */
  private String mValue;

  /** Tag color */
  private int mColor;

  /** Tag selected */
  private boolean mSelected;

  public Tag(int id, String value, int color)
  {
    this.mId = id;
    this.mValue = value;
    this.mColor = color;
    this.mSelected = true;
  }

  public int getId()
  {
    return this.mId;
  }

  public String getValue()
  {
    return this.mValue;
  }

  public int getColor()
  {
    return this.mColor;
  }

  public void setSelected(boolean isSelected)
  {
    this.mSelected = isSelected;
  }

  public boolean isSelected()
  {
    return this.mSelected;
  }

  public boolean equals(Object o)
  {
    if (o instanceof Tag)
    {
      Tag t = (Tag) o;
      /* Tag equality is defined by id equality */
      return t.mId == this.mId;
    }
    else
      return false;
  }

  public boolean hasChanged(Object o)
  {
    if (o instanceof Tag)
    {
      Tag t = (Tag) o;
      /* A tag has changed if one of its attributes(except id) has changed */
      return !t.getValue().equals(this.mValue) || t.getColor() != this.mColor
        || t.mSelected != this.mSelected;
    }
    else
      return false;
  }

  public String toString()
  {
    return this.mValue;
  }
}

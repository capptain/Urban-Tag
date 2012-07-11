package com.ubikod.urbantag.model;


public class Tag
{
  private int mId;
  private String mValue;
  private int mColor;
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
      return t.getId() == this.mId;
    }
    else
      return false;
  }

  public boolean hasChanged(Object o)
  {
    if (o instanceof Tag)
    {
      Tag t = (Tag) o;
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

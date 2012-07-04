package com.ubikod.urbantag.model;

public class Tag
{
  private int id;
  private String value;
  private int color;
  private boolean selected;

  public Tag(int id, String value, int color)
  {
    this.id = id;
    this.value = value;
    this.color = color;
    this.selected = true;
  }

  public int getId()
  {
    return this.id;
  }

  public String getValue()
  {
    return this.value;
  }

  public int getColor()
  {
    return this.color;
  }

  public void setSelected(boolean isSelected)
  {
    this.selected = isSelected;
  }

  public boolean isSelected()
  {
    return this.selected;
  }

  public boolean equals(Object o)
  {
    if (o instanceof Tag)
    {
      Tag t = (Tag) o;
      return t.getId() == this.id;
    }
    else
      return false;
  }

  public boolean hasChanged(Object o)
  {
    if (o instanceof Tag)
    {
      Tag t = (Tag) o;
      return t.getValue().equals(this.value) && t.getColor() == this.color;
    }
    else
      return false;
  }

  public String toString()
  {
    return this.value;
  }
}

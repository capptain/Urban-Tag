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

package com.ubikod.urbantag.model;

import java.util.List;

public class Content
{
  private int id;
  private String name;
  private int startDate;
  private int endDate;
  private Place place;
  private Tag mainTag;
  private List<Tag> allTags = null;

  public Content(int id, String name, int startDate, int endDate, Place place, Tag mainTag,
    List<Tag> allTags)
  {
    this.id = id;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.place = place;
    this.allTags = allTags;
    this.mainTag = mainTag;
  }

  public int getId()
  {
    return this.id;
  }

  public String getName()
  {
    return this.name;
  }

  public int getStartDate()
  {
    return this.startDate;
  }

  public int getEndDate()
  {
    return this.endDate;
  }

  public Place getPlace()
  {
    return this.place;
  }

  public List<Tag> getAllTags()
  {
    return this.allTags;
  }

  public Tag getTag()
  {
    return this.mainTag;
  }
}

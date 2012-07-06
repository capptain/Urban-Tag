package models.data;

public class CampaignContentData
{
  String title;
  long[] tags;
  long mainTag;
  long idInfo;
  CampaignPlaceData place;
  long startDate = -1;
  long endDate = -1;

  public CampaignContentData(String title, long[] tags, long mainTag, long idInfo,
    CampaignPlaceData place)
  {
    super();
    this.title = title;
    this.tags = tags;
    this.mainTag = mainTag;
    this.idInfo = idInfo;
    this.place = place;
  }

  public CampaignContentData(String title, long[] tags, long mainTag, long idInfo,
    CampaignPlaceData place, long startDate, long endDate)
  {
    super();
    this.title = title;
    this.tags = tags;
    this.mainTag = mainTag;
    this.idInfo = idInfo;
    this.place = place;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public long[] getTags()
  {
    return tags;
  }

  public void setTags(long[] tags)
  {
    this.tags = tags;
  }

  public long getMainTag()
  {
    return mainTag;
  }

  public void setMainTag(long mainTag)
  {
    this.mainTag = mainTag;
  }

  public long getIdInfo()
  {
    return idInfo;
  }

  public void setIdInfo(long idInfo)
  {
    this.idInfo = idInfo;
  }

  public CampaignPlaceData getPlace()
  {
    return place;
  }

  public void setPlace(CampaignPlaceData place)
  {
    this.place = place;
  }

  public long getStartDate()
  {
    return startDate;
  }

  public void setStartDate(long startDate)
  {
    this.startDate = startDate;
  }

  public long getEndDate()
  {
    return endDate;
  }

  public void setEndDate(long endDate)
  {
    this.endDate = endDate;
  }

  public static class CampaignPlaceData
  {
    long id;
    String name;
    double lon;
    double lat;
    long[] tags;
    long mainTag;

    public CampaignPlaceData(long id, String name, double lon, double lat, long[] tags, long maintag)
    {
      super();
      this.id = id;
      this.name = name;
      this.lon = lon;
      this.lat = lat;
      this.tags = tags;
      this.mainTag = maintag;
    }

    public long getId()
    {
      return id;
    }

    public void setId(long id)
    {
      this.id = id;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public double getLon()
    {
      return lon;
    }

    public void setLon(double lon)
    {
      this.lon = lon;
    }

    public double getLat()
    {
      return lat;
    }

    public void setLat(double lat)
    {
      this.lat = lat;
    }

    public long[] getTags()
    {
      return tags;
    }

    public void setTags(long[] tags)
    {
      this.tags = tags;
    }

    public long getMainTag()
    {
      return mainTag;
    }

    public void setMainTag(long maintag)
    {
      this.mainTag = maintag;
    }

  }
}

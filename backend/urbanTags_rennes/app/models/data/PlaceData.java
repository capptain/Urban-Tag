package models.data;

public class PlaceData
{

  private long id = -1, idOwner = -1;
  private String name;
  private double longitude, latitude;
  private int radius, accuracy, expiration;
  private int[] tags;
  private int mainTag;

  public long getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }

  public double getLongitude()
  {
    return longitude;
  }

  public double getLatitude()
  {
    return latitude;
  }

  public int getRadius()
  {
    return radius;
  }

  public int getAccuracy()
  {
    return accuracy;
  }

  public int getExpiration()
  {
    return expiration;
  }

  public int[] getTags()
  {
    return tags;
  }

  public int getMainTag()
  {
    return mainTag;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public void setLongitude(double longitude)
  {
    this.longitude = longitude;
  }

  public void setLatitude(double latitude)
  {
    this.latitude = latitude;
  }

  public void setRadius(int radius)
  {
    this.radius = radius;
  }

  public void setAccuracy(int accuracy)
  {
    this.accuracy = accuracy;
  }

  public void setExpiration(int expiration)
  {
    this.expiration = expiration;
  }

  public void setTags(int[] tags)
  {
    this.tags = tags;
  }

  public void setMainTag(int mainTag)
  {
    this.mainTag = mainTag;
  }

  public long getIdOwner()
  {
    return idOwner;
  }

  public void setIdOwner(long idOwner)
  {
    this.idOwner = idOwner;
  }
}

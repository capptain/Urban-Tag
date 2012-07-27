package models.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CampaignData
{
  private long id;
  private String state;
  private String name;
  private String type;
  private String deliveryTime;
  private String startTime = null, endTime = null;
  private String body;
  private String timezone = "Europe/Paris";

  private AudienceData audience;

  public CampaignData(String name, String type, String deliveryTime, long startDate, long endDate,
    String body, double lon, double lat, float rad, int expiration, int accuracy)
  {
    super();
    this.name = name;
    this.type = type;
    this.deliveryTime = deliveryTime;
    this.body = body;

    if (startDate >= 0)
    {
      Date tmpDate = new Date();
      tmpDate.setTime(startDate);
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm'Z'");
      this.startTime = dateFormat.format(tmpDate);
    }

    if (endDate >= 0)
    {
      Date tmpDate = new Date();
      tmpDate.setTime(endDate);
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm'Z'");
      this.endTime = dateFormat.format(tmpDate);
    }

    String criterionName = "Geofencing";
    HashMap<String, Criterion> criteria = new HashMap<String, Criterion>();
    criteria.put(criterionName, new Criterion("geo-fencing", new POI[] { new POI("poi", expiration,
      accuracy, new Shape("circle", lon, lat, Math.round(rad))) }));

    this.audience = new AudienceData(criterionName, criteria);
  }

  public long getId()
  {
    return id;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public String getState()
  {
    return state;
  }

  public void setState(String state)
  {
    this.state = state;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getDeliveryTime()
  {
    return deliveryTime;
  }

  public void setDeliveryTime(String deliveryTime)
  {
    this.deliveryTime = deliveryTime;
  }

  public String getBody()
  {
    return body;
  }

  public void setBody(String body)
  {
    this.body = body;
  }

  // // public AudienceData getAudience()
  // // {
  // // return audience;
  // // }
  //
  // // public void setAudience(AudienceData audience)
  // // {
  // // this.audience = audience;
  // // }

  public String getStartTime()
  {
    return startTime;
  }

  public void setStarTime(String startDate)
  {
    this.startTime = startDate;
  }

  public String getEndTime()
  {
    return endTime;
  }

  public void setEndTime(String endDate)
  {
    this.endTime = endDate;
  }

  public void setStartTime(String startTime)
  {
    this.startTime = startTime;
  }

  public String getTimezone()
  {
    return timezone;
  }

  public void setTimezone(String timezone)
  {
    this.timezone = timezone;
  }

  public static class AudienceData
  {
    private String expression;
    private HashMap<String, Criterion> criteria;

    public AudienceData(String expression, HashMap<String, Criterion> criteria)
    {
      this.expression = expression;
      this.criteria = criteria;
    }

    public String getExpression()
    {
      return expression;
    }

    public void setExpression(String expression)
    {
      this.expression = expression;
    }

    public HashMap<String, Criterion> getCritera()
    {
      return criteria;
    }

    public void setCritera(HashMap<String, Criterion> critera)
    {
      this.criteria = critera;
    }

  }

  public static class Criterion
  {
    private String type;
    private POI[] poiList;

    public Criterion(String type, POI[] poiList)
    {
      this.type = type;
      this.poiList = poiList;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
    }

    public POI[] getPoiList()
    {
      return poiList;
    }

    public void setPoiList(POI[] poiList)
    {
      this.poiList = poiList;
    }
  }

  public static class POI
  {
    private String name;
    private int expiration;
    private int accuracyThreshold;
    private Shape shape;

    public POI(String name, int expiration, int accuracy, Shape shape)
    {
      this.name = name;
      this.expiration = expiration;
      this.accuracyThreshold = accuracy;
      this.shape = shape;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public int getExpiration()
    {
      return expiration;
    }

    public void setExpiration(int expiration)
    {
      this.expiration = expiration;
    }

    public int getAccuracy()
    {
      return accuracyThreshold;
    }

    public void setAccuracy(int accuracy)
    {
      this.accuracyThreshold = accuracy;
    }

    public Shape getShape()
    {
      return shape;
    }

    public void setShape(Shape shape)
    {
      this.shape = shape;
    }

  }

  public static class Shape
  {
    private String type;
    private double lon;
    private double lat;
    private int rad;

    public Shape(String type, double lon, double lat, int rad)
    {
      this.type = type;
      this.lon = lon;
      this.lat = lat;
      this.rad = rad;
    }

    public String getType()
    {
      return type;
    }

    public void setType(String type)
    {
      this.type = type;
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

    public int getRad()
    {
      return rad;
    }

    public void setRad(int rad)
    {
      this.rad = rad;
    }

  }
}

package models.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InfoData
{
  private long id = -1;
  private String title = "";
  private String startDate = "", endDate = "";
  private long[] tags = null;
  private long mainTag = -1;
  private String content = null;
  private long placeId = -1;
  private String type = "";
  private long addedAt = -1;

  public long getId()
  {
    return id;
  }

  public String getTitle()
  {
    return title;
  }

  public String getStartDate()
  {
    return startDate;
  }

  public void setStartDate(String startDate)
  {
    this.startDate = startDate;
  }

  public String getEndDate()
  {
    return endDate;
  }

  public void setEndDate(String endDate)
  {
    this.endDate = endDate;
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

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public long getPlaceId()
  {
    return this.placeId;
  }

  public void setPlaceId(long id)
  {
    this.placeId = id;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public long getAddedAt()
  {
    return addedAt;
  }

  public void setAddedAt(long addedAt)
  {
    this.addedAt = addedAt;
  }

  public Date getConvertedStartDate() throws Exception
  {

    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    return dateFormat.parse(this.startDate);
  }

  public Date getConvertedEndDate() throws Exception
  {
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    return dateFormat.parse(this.endDate);
  }
}

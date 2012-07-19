package models;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.Tag.TagNotFoundException;
import models.check.attribute.InfoTitleCheck;
import models.check.attribute.MainTagCheck;
import models.data.CampaignContentData;
import models.data.CampaignContentData.CampaignPlaceData;
import models.data.CampaignData;
import models.data.InfoData;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import reach.ReachWrapper;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

@Entity
public class Info extends GenericModel
{
  @Id
  @GeneratedValue
  @Expose
  public Long id;

  @Required
  @Expose
  @CheckWith(InfoTitleCheck.class)
  public String title;

  @Required
  @Lob
  @Expose
  public String content;

  @Expose
  public Date startDate;
  @Expose
  public Date endDate;

  @Required
  @Expose
  public Date addedAt;

  @Required
  @ManyToOne(cascade = CascadeType.PERSIST)
  @Expose
  public Place place;

  @ManyToMany(cascade = CascadeType.PERSIST)
  @Expose
  public Set<Tag> tags;

  @CheckWith(MainTagCheck.class)
  @ManyToOne(cascade = CascadeType.PERSIST)
  @Expose
  public Tag mainTag;

  public long campaignId;

  public Info(Place place, String title, String content)
  {
    this(place, title, content, null, null);
  }

  public Info()
  {
  }

  public Info(Place place, String title, String content, Date startDate, Date endDate)
  {
    this.place = place;
    this.title = title;
    this.content = content;
    this.startDate = startDate;
    this.endDate = endDate;
    this.addedAt = new Date();
    this.tags = new TreeSet<Tag>();
    this.mainTag = null;
  }

  public String toString()
  {
    return title;
  }

  public Boolean isActive()
  {
    try
    {
      return ReachWrapper.getDataPushDetails(campaignId).getState().equals("in-progress");
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

  public Boolean isFinished()
  {
    try
    {
      return ReachWrapper.getDataPushDetails(campaignId).getState().equals("finished");
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Basic tag function, try to add a new tag to the object without making it the main tag.
   * @param tag Desired tag value.
   * @return Tagged object.
   * @throws TagNotFoundException If the tag is not correct, an instance of this class is raised.
   * @see Place#tagItWith(String, boolean)
   */
  public Info tagItWith(String tag) throws TagNotFoundException
  {
    return tagItWith(tag, false);
  }

  public Info tagItWith(Tag tag, boolean isMainTag)
  {
    if (!tags.contains(tag))
    {
      tags.add(tag);

      if (isMainTag || mainTag == null)
        mainTag = tag;
    }

    return this;
  }

  /**
   * Try to add a tag to the Info. If the Info is already tagged with the passed value, do nothing.
   * If the tag is not correct, raise an exception.
   * @param tag Desired tag value.
   * @param isMainTag Indicate if the new tag must be the main one. If a main tag already exist,
   *          replace it.
   * @return Tagged object.
   * @throws TagNotFoundException If the tag is not correct, and instance of this class is raised.
   */
  public Info tagItWith(String tag, boolean isMainTag) throws TagNotFoundException
  {
    // Retrieve tag
    Tag savedTag = Tag.find("byName", tag).first();

    // Raise an exception if incorrect tag value
    if (savedTag == null)
      throw new TagNotFoundException(tag);

    // Check that the place is not already tagged with the passed value
    if (!tags.contains(savedTag))
    {
      // Add tag to the place
      tags.add(savedTag);

      // Make it the main tag if asked or if first tag
      if (isMainTag || mainTag == null)
        mainTag = savedTag;
    }

    // Return tagged object
    return this;
  }

  public Info removeTag(String tag)
  {
    if (tags.contains(tag))
      tags.remove(tag);

    return this;
  }

  public Info removeAllTags()
  {
    tags = new TreeSet<Tag>();

    return this;
  }

  @Override
  public Info save()
  {
    /* if Edition */
    if (id != null)
    {
      /* Delete old data-push campaign */
      try
      {
        boolean isFinished = isFinished();
        boolean isActive = isActive();

        if (isFinished || (isActive && ReachWrapper.finishDataPush(campaignId))
          || (!isFinished && !isActive && ReachWrapper.suspendDataPush(campaignId)))
        {
          {
            ReachWrapper.deleteDataPush(this.campaignId);
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        return null;
      }
    }

    if (startDate != null)
    {
      Calendar startCalendar = Calendar.getInstance();
      startCalendar.setTime(startDate);
      Calendar now = Calendar.getInstance();

      if (startCalendar.before(now))
      {
        startCalendar = now;
        startCalendar.add(Calendar.MINUTE, 1);
        startDate = startCalendar.getTime();
      }
    }

    boolean wasNew = (id == null);
    try
    {
      /* Need to associate an id to the Info in order to be able to create a campaign */
      if (id == null)
        id = ((Info) super.save()).id;

      /* Create new datapush campaign */
      CampaignData campaign = this.getCampaign();
      long campaignId = ReachWrapper.createDataPush(campaign);

      /* Save Info with the new campaign's ID */
      this.campaignId = campaignId;
      return super.save();
    }
    /* If campaign creation fail, delete Info object */
    catch (Exception e)
    {
      e.printStackTrace();

      if (wasNew)
        super.delete();

      return null;
    }
  }

  @Override
  public Info delete()
  {
    if (this.campaignId > 0)
    {
      try
      {
        if (isFinished() || (isActive() && ReachWrapper.finishDataPush(campaignId))
          || (!isFinished() && !isActive() && ReachWrapper.suspendDataPush(campaignId)))
        {
          {
            ReachWrapper.deleteDataPush(this.campaignId);
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        return null;
      }
    }

    return super.delete();
  }

  public CampaignData getCampaign()
  {
    /* place data */
    CampaignPlaceData placeData = new CampaignPlaceData(this.place.id, this.place.name,
      this.place.longitude, this.place.latitude, this.place.tags.toArray(new Tag[0]),
      this.place.mainTag);

    /* content */
    CampaignContentData contentObj = new CampaignContentData(this.title,
      this.tags.toArray(new Tag[0]), this.mainTag, this.id, placeData);

    long startDate = -1, endDate = -1;
    if (this.startDate != null && this.endDate != null)
    {
      startDate = this.startDate.getTime();
      endDate = this.endDate.getTime();
      contentObj.setStartDate(this.startDate.getTime());
      contentObj.setEndDate(this.endDate.getTime());
    }

    String content = new Gson().toJson(contentObj, CampaignContentData.class);

    int expirationTime, accuracyThreshold;

    if (this.place.accuracy.equals("low"))
    {
      expirationTime = 60;
      accuracyThreshold = 1000;
    }
    else if (this.place.accuracy.equals("high"))
    {
      expirationTime = 1;
      accuracyThreshold = 10;
    }
    else
    {
      expirationTime = 10;
      accuracyThreshold = 100;
    }

    /* campaign creation */
    CampaignData campaign = new CampaignData(this.id + "", "text/plain", "when-started", startDate,
      endDate, content, this.place.longitude, this.place.latitude, this.place.radius,
      expirationTime, accuracyThreshold);

    return campaign;
  }

  public Info setData(InfoData data) throws Exception
  {
    try
    {
      this.title = data.getTitle();
      this.content = data.getContent();
      if (!data.getStartDate().isEmpty() && !data.getEndDate().isEmpty())
      {
        startDate = data.getConvertedStartDate();
        endDate = data.getConvertedEndDate();
      }
      else
      {
        startDate = null;
        endDate = null;
      }
      this.addedAt = new Date(data.getAddedAt());
      this.place = Place.findById(data.getPlaceId());
      this.removeAllTags();
      for (int i = 0; i < data.getTags().length; i++)
      {
        long tagId = data.getTags()[i];
        boolean isMain = (tagId == data.getMainTag());
        Tag tag = Tag.findById(tagId);
        this.tagItWith(tag, isMain);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw e;
    }
    return this;
  }
}

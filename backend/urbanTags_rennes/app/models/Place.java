package models;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.Tag.TagNotFoundException;
import models.check.attribute.MainTagCheck;
import models.check.attribute.PlaceAccuracyCheck;
import models.data.PlaceData;
import play.data.validation.CheckWith;
import play.data.validation.Max;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;

import com.google.gson.annotations.Expose;

/**
 * A place is a circular area to which pushed informations are connected. It can have an device
 * location accuracy threshold and device location time threshold. A place can be tagged with
 * multiple tags. It can have a main tag which defines the display color of the place.
 * @author Guillaume PANNETIER
 */
@Entity
public class Place extends GenericModel
{
  @Id
  @GeneratedValue
  @Expose
  public Long id;

  @Required
  @Expose
  @Unique
  public String name;

  /**
   * Longitude of the center of the circular area. (EPSG = 4326)
   */
  @Required
  @Expose
  @Max(180)
  @Min(-180)
  public double longitude;

  /**
   * Latitude of the center of the circular area. (EPSG = 4326)
   */
  @Required
  @Expose
  @Max(90)
  @Min(-90)
  public double latitude;

  /**
   * Radius (in meters) of the circular area
   */
  @Required
  @Expose
  @Max(300)
  @Min(10)
  public int radius;

  /**
   * Device location accuracy.
   */
  @Expose
  @CheckWith(PlaceAccuracyCheck.class)
  public String accuracy;

  /**
   * Author of the place
   */
  @Required
  @ManyToOne
  @Expose
  public User owner;

  /**
   * Tags of the place.
   */
  @ManyToMany(cascade = CascadeType.PERSIST)
  @Expose
  public Set<Tag> tags;

  /**
   * Main tag of the place. Defines the display color of the place.
   */
  @CheckWith(MainTagCheck.class)
  @ManyToOne(cascade = CascadeType.PERSIST)
  @Expose
  public Tag mainTag;

  public Place()
  {
  }

  /**
   * Basic constructor of the class, doesn't define thresholds.
   * @param owner {@link Place#owner}
   * @param name {@link Place#name}
   * @param longitude {@link Place#longitude}
   * @param latitude {@link Place#latitude}
   * @param radius {@link Place#radius}
   */
  public Place(User owner, String name, double longitude, double latitude, int radius)
  {
    // Call plain constructor with null values for thresholds

    this(owner, name, longitude, latitude, radius, "medium");
  }

  /**
   * Plain constructor of the class, with thresholds definition.
   * @param owner {@link Place#owner}
   * @param name {@link Place#name}
   * @param longitude {@link Place#longitude}
   * @param latitude {@link Place#latitude}
   * @param radius {@link Place#radius}
   * @param accuracy {@link Place#accuracy}
   */
  public Place(User owner, String name, double longitude, double latitude, int radius,
    String accuracy)
  {
    // Set attributes

    this.owner = owner;
    this.name = name;
    this.longitude = longitude;
    this.latitude = latitude;
    this.radius = radius;
    this.mainTag = null;
    this.tags = new TreeSet<Tag>();
    this.accuracy = accuracy;
  }

  public Place removeTag(String tag)
  {
    if (tags.contains(tag))
      tags.remove(tag);

    return this;
  }

  public Place removeAllTags()
  {
    tags = new TreeSet<Tag>();

    return this;
  }

  /**
   * Basic tag function, try to add a new tag to the object without making it the main tag.
   * @param tag Desired tag value.
   * @return Tagged object.
   * @throws TagNotFoundException If the tag is not correct, an instance of this class is raised.
   * @see Place#tagItWith(String, boolean)
   */
  public Place tagItWith(String tag) throws TagNotFoundException
  {
    return tagItWith(tag, false);
  }

  public Place tagItWith(Tag tag, boolean isMainTag)
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
   * Try to add a tag to the Place. If the place is already tagged with the passed value, do
   * nothing. If the tag is not correct, raise an exception.
   * @param tag Desired tag value.
   * @param isMainTag Indicate if the new tag must be the main one. If a main tag already exist,
   *          replace it.
   * @return Tagged object.
   * @throws TagNotFoundException If the tag is not correct, and instance of this class is raised.
   */
  public Place tagItWith(String tag, boolean isMainTag) throws TagNotFoundException
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

  /*
   * (non-Javadoc)
   * @see play.db.jpa.JPABase#toString()
   */
  public String toString()
  {
    return name;
  }

  public Place setData(PlaceData data)
  {
    this.name = data.getName();
    this.longitude = data.getLongitude();
    this.latitude = data.getLatitude();
    this.radius = data.getRadius();
    this.accuracy = data.getAccuracy();
    this.owner = User.findById(data.getIdOwner());

    this.removeAllTags();
    for (int i = 0; i < data.getTags().length; i++)
    {
      long tagId = data.getTags()[i];
      boolean isMain = (tagId == data.getMainTag());
      Tag tag = Tag.findById(tagId);
      this.tagItWith(tag, isMain);
    }

    return this;
  }

  @Override
  public Place save()
  {
    Place result = super.save();

    if (id != null)
    {
      List<Info> infos = Info.find("byPlace", this).fetch();
      for (Info info : infos)
      {
        if (info.startDate == null && info.endDate == null)
        {
          info.mainTag = this.mainTag;
          info.tags = this.tags;
          info.title = this.name;
        }
        info.save();
      }
    }

    return result;
  }

  @Override
  public Place delete()
  {
    List<Info> infos = Info.find("byPlace", this).fetch();
    for (Info info : infos)
    {
      info.delete();
    }

    return super.delete();
  }
}

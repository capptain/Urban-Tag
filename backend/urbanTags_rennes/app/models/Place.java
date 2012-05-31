package models;

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.Tag.TagNotFoundException;
import models.check.attribute.MainTagCheck;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * A place is a circular area to which pushed informations are connected. It can have an device
 * location accuracy threshold and device location time threshold. A place can be tagged with
 * multiple tags. It can have a main tag which defines the display color of the place.
 * @author Guillaume PANNETIER
 */
@Entity
public class Place extends Model
{
  @Required
  public String name;

  /**
   * Longitude of the center of the circular area. (EPSG = 4326)
   */
  @Required
  public double longitude;

  /**
   * Latitude of the center of the circular area. (EPSG = 4326)
   */
  @Required
  public double latitude;

  /**
   * Radius (in meters) of the circular area
   */
  @Required
  public int radius;

  /**
   * Optional device location threshold (in meters).
   */
  public int accuracy;

  /**
   * Optional device location time (in minutes).
   */
  public int expiration;

  /**
   * Author of the place
   */
  @Required
  @ManyToOne
  public User owner;

  /**
   * Tags of the place.
   */
  @ManyToMany(cascade = CascadeType.PERSIST)
  public Set<Tag> tags;

  /**
   * Main tag of the place. Defines the display color of the place.
   */
  @CheckWith(MainTagCheck.class)
  @ManyToOne(cascade = CascadeType.PERSIST)
  public Tag mainTag;

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

    this(owner, name, longitude, latitude, radius, -1, -1);
  }

  /**
   * Plain constructor of the class, with thresholds definition.
   * @param owner {@link Place#owner}
   * @param name {@link Place#name}
   * @param longitude {@link Place#longitude}
   * @param latitude {@link Place#latitude}
   * @param radius {@link Place#radius}
   * @param accuracy {@link Place#accuracy}
   * @param expiration {@link Place#expiration}
   */
  public Place(User owner, String name, double longitude, double latitude, int radius,
    int accuracy, int expiration)
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
    this.expiration = expiration;
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
}

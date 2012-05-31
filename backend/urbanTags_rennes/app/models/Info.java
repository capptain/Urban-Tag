package models;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import models.Tag.TagNotFoundException;
import models.check.attribute.MainTagCheck;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Info extends Model
{
  @Required
  public String title;

  @Required
  @Lob
  public String content;

  public Date startDate;
  public Date endDate;
  public Date addedAt;

  @Required
  @ManyToOne
  public Place place;

  @ManyToMany(cascade = CascadeType.PERSIST)
  public Set<Tag> tags;

  @CheckWith(MainTagCheck.class)
  public Tag mainTag;

  public Info(Place place, String title, String content)
  {
    this(place, title, content, null, null);
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
    if (startDate == null && endDate == null)
      return true;
    else
      return new Date().after(startDate) && new Date().before(endDate);

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
}

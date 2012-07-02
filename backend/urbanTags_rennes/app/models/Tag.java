package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;

import com.google.gson.annotations.Expose;

@Entity
public class Tag extends GenericModel implements Comparable<Tag>
{
  public static class TagNotFoundException extends Exception
  {
    public TagNotFoundException(String tag)
    {
      super("The tag '" + tag + "' is not a correct tag.");
    }
  }

  @Id
  @GeneratedValue
  @Expose
  public Long id;

  @Required
  @Expose
  @Unique
  public String name;

  @Required
  @Expose
  @Unique
  public String color;

  public Tag(String name, String color)
  {
    this.name = name;
    this.color = color;
  }

  @Override
  public int compareTo(Tag otherTag)
  {
    return name.compareTo(otherTag.name);
  }

  public String toString()
  {
    return name;
  }
}

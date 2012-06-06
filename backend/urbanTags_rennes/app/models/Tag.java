package models;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Tag extends Model implements Comparable<Tag>
{
  public static class TagNotFoundException extends Exception
  {
    public TagNotFoundException(String tag)
    {
      super("The tag '" + tag + "' is not a correct tag.");
    }
  }

  @Required
  public String name;

  @Required
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

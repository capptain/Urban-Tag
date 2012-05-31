import models.Place;
import models.Tag;
import models.Tag.TagNotFoundException;
import models.User;

import org.junit.Test;

import play.mvc.Before;
import play.test.Fixtures;
import play.test.UnitTest;

public class TagTest extends UnitTest
{

  @Before
  public void setUp()
  {
    Fixtures.deleteDatabase();
  }

  @Test
  public void TagOnPlaceTest()
  {

  }

  @Test
  public void TagOnInfoTest()
  {
    // Create a new User
    User user = new User("bob@gmail.com", "secret", "bob", "basic").save();

    // Create a new Place
    Place place = new Place(user, "Eiffel Tower", 2.294444, 48.858333, 50).save();

    new Tag("Music").save();

    try
    {
      place.tagItWith("Music");
    }
    catch (TagNotFoundException e1)
    {
      e1.printStackTrace();
      fail("The 'Music' tag application should have succeed");
    }

    try
    {
      place.tagItWith("Architecture");
      fail("The tag 'Architecture' application should have failed");
    }
    catch (TagNotFoundException e)
    {

    }
  }

}

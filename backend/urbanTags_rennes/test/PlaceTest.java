import java.util.List;

import models.Place;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class PlaceTest extends UnitTest
{

  @Before
  public void setup()
  {
    Fixtures.deleteDatabase();
  }

  @Test
  public void createAndRetrievePlace()
  {
    // Create a new User
    User user = new User("bob@gmail.com", "secret", "bob", "basic").save();

    // Create a new place
    Place place = new Place(user, "Eiffel tower", 2.294444, 48.858333, 50, "low").save();

    // Retrive user's places
    List<Place> userPlaces = Place.find("byOwner", user).fetch();

    // Test that the user's place is the previously created one
    assertEquals(1, userPlaces.size());
    assertEquals(userPlaces.get(0), place);
  }

}

import java.util.Calendar;

import models.Info;
import models.Place;
import models.User;

import org.junit.Test;

import play.mvc.Before;
import play.test.Fixtures;
import play.test.UnitTest;

public class InformationTest extends UnitTest
{

  @Before
  public void setup()
  {
    Fixtures.deleteDatabase();
  }

  @Test
  public void CreateAndRetrieveInformation()
  {
    // Create a new User
    User user = new User("bob@gmail.com", "secret", "bob", "basic").save();

    // Create a new Place
    Place place = new Place(user, "Eiffel Tower", 2.294444, 48.858333, 50).save();

    // Create a new constant Info
    Info cstInfo = new Info(place, "Constant information",
      "This is a constant information's content.").save();

    // Create start date
    Calendar before = Calendar.getInstance();
    before.add(Calendar.DAY_OF_MONTH, -1);

    // Create end date
    Calendar after = Calendar.getInstance();
    after.add(Calendar.DAY_OF_MONTH, 1);

    // Create an active event Info
    Info activeEvtInfo = new Info(place, "Event information",
      "This is an inactive event information's content", before.getTime(), after.getTime()).save();

    // Create start date
    before = Calendar.getInstance();
    before.add(Calendar.DAY_OF_MONTH, -2);

    // Create end date
    after = Calendar.getInstance();
    after.add(Calendar.DAY_OF_MONTH, -1);

    // Create an inactive event Info
    Info inactiveEvtInfo = new Info(place, "Event information",
      "This is an active event information's content", before.getTime(), after.getTime()).save();

    // Test isActive function
    assertTrue(cstInfo.isActive());
    assertTrue(activeEvtInfo.isActive());
    assertFalse(inactiveEvtInfo.isActive());

    // Count info for the place
    assertEquals(3, Info.find("byPlace", place).fetch().size());
  }
}

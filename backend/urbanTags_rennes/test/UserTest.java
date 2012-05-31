import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class UserTest extends UnitTest
{

  @Before
  public void setup()
  {
    Fixtures.deleteDatabase();
  }

  @Test
  public void createAndRetrieveUser()
  {
    // Create a new User
    new User("bob@gmail.com", "secret", "bob", "basic").save();

    // Retrieve it
    User bob = User.find("byEmail", "bob@gmail.com").first();

    // Test
    assertNotNull(bob);
  }

  @Test
  public void tryConnectUser()
  {
    // Create a new User
    new User("bob@gmail.com", "secret", "bob", "basic").save();

    // Try to connect
    assertNotNull(User.connect("bob@gmail.com", "secret"));

    // Try with wrong parameters
    assertNull(User.connect("bob", "secret"));
    assertNull(User.connect("bob@gmail.com", "password"));
    assertNull(User.connect("john@gmail.com", "secret"));
  }

  @Test
  public void editUser()
  {
    // Create a new User
    User user = new User("bob@gmail.com", "secret", "bob", "basic").save();

    // Save bob's ID
    long id = user.getId();

    // Retrieve profile with Bob's ID
    User savedUser = User.findById(id);

    // Check attributes
    assertEquals("bob@gmail.com", savedUser.email);
    assertEquals("secret", savedUser.password);
    assertEquals("bob", savedUser.username);
    assertEquals("basic", savedUser.role);

    // Edit profile
    user = User.findById(id);
    user.username = "john";
    user.save();

    // Retrieve profile with Bob/John's ID
    savedUser = User.findById(id);

    // Check attributes
    assertEquals("bob@gmail.com", savedUser.email);
    assertEquals("secret", savedUser.password);
    assertNotSame("bob", savedUser.username);
    assertEquals("john", savedUser.username);
    assertEquals("basic", savedUser.role);
  }
}

package models;

import javax.persistence.Entity;

import models.check.security.UserRoleCheck;
import play.data.validation.CheckWith;
import play.data.validation.Email;
import play.data.validation.Password;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;

/**
 * A User object represents an account. It contains login, access and display informations.
 * @author Guillaume PANNETIER
 */
@Entity
public class User extends Model
{
  /**
   * Email of the user. The email is used to log in.
   */
  @Email
  @Required
  @Unique
  public String email;

  /**
   * Password of the user.
   */
  @Password
  @Required
  public String password;

  /**
   * Displayed username
   */
  @Required
  @Unique
  public String username;

  /**
   * Access role
   */
  @CheckWith(UserRoleCheck.class)
  public String role;

  /**
   * Constructor of a new User.
   * @param email {@link User#email}
   * @param password {@link User#password}
   * @param username {@link User#username}
   * @param role {@link User#role}
   */
  public User(String email, String password, String username, String role)
  {
    // Set attributes with parameters values.
    this.email = email;
    this.password = password;
    this.username = username;
    this.role = role;
  }

  /**
   * Try to connect to the User account which matchs with the log in informations.
   * @param email Email of the account
   * @param password Password of the account
   * @return User object if informations are correct, otherwise null.
   */
  public static User connect(String email, String password)
  {
    return find("byEmailAndPassword", email, password).first();
  }

  /*
   * (non-Javadoc)
   * @see play.db.jpa.JPABase#toString()
   */
  public String toString()
  {
    return username;
  }
}

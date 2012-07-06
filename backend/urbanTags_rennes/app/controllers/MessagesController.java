package controllers;

import play.i18n.Messages;
import play.mvc.Controller;

public class MessagesController extends Controller
{

  public static void getMessage(String messageName)
  {
    renderText(Messages.get(messageName));
  }
}

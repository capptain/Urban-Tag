package controllers;

import play.mvc.Controller;

public class PlaceSheet extends Controller
{
  public static void getTemplate()
  {
    render("Place/sheet/template.html");
  }
}

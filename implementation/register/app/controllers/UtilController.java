package controllers;

import controllers.*;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by David Goeth on 07.10.2017.
 */
public class UtilController extends Controller {

    public  Result getPortAction() throws MalformedURLException {
      Call call = controllers.routes.UtilController.getPortAction();
      URL absoluteRequestURL = new URL(call.absoluteURL(request()));
      int port = absoluteRequestURL.getPort();
      //Call call = routes.UtilController.;
     // routes.
      //String absoluteUrl = call.absoluteURL(request());
      //Here absoluteUrl would be like http://localhost:9002/...., 9002 would be the port.
      return ok(Json.toJson(new Integer(port)));
    }
}

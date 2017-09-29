package controllers.response;

import de.unipassau.medspace.register.Results.Add;

/**
 * Created by David Goeth on 29.09.2017.
 */
public class AddResultResponse extends ResultResponse {

  public AddResultResponse(Add add) {
    switch(add) {
      case NULL_NOT_VALID:
        result = false;
        response = "Missing or wrong data was send.";
        break;
      case SUCCESS:
        result = true;
        response = "Successfully added a new datasource or updated an existing one.";
        break;
      case NO_SUCCESS_NEWER_VERSION_EXISTS:
        result = false;
        response = "The datasource couldn't be updated as there exists a newer version of it.";
        break;
        default:
          throw new IllegalStateException("Unknown enum: " + add.toString());
    }
  }
}
package controllers.response;

import de.unipassau.medspace.register.Results.NoResponse;

/**
 * Created by David Goeth on 29.09.2017.
 */
public class NoResponseResultResponse extends ResultResponse {

  public NoResponseResultResponse(NoResponse noResponse) {
    switch(noResponse) {
      case REMOVED_DATASOURCE:
        result = true;
        response = "Removed not responding datasource.";
        break;
      case NULL_NOT_VALID:
        result = false;
        response = "Missing or wrong data was send.";
        break;
      case DATASOURCE_NOT_FOUND:
        result = false;
        response = "Datasource isn't registered, thus nothing done.";
        break;
      case COOL_DOWN_ACTIVE:
        result = false;
        response = "Datasource wasn't removed. Its cooldown is still active.";
        break;
      default:
        throw new IllegalStateException("Unknown enum: " + noResponse.toString());
    }
  }
}

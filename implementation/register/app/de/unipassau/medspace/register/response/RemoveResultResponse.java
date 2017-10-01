package de.unipassau.medspace.register.response;

import de.unipassau.medspace.register.Results.Remove;

/**
 * Created by David Goeth on 29.09.2017.
 */
public class RemoveResultResponse extends ResultResponse {

  public RemoveResultResponse(Remove remove) {
    switch(remove) {
      case SUCCESS:
        result = true;
        response = "Successfully removed the datasource.";
        break;
      case DATASOURCE_NOT_FOUND:
        result = false;
        response = "Datasource not found.";
        break;
      case NULL_NOT_VALID:
        result = false;
        response = "Missing or wrong data was send.";
        break;
      default:
        throw new IllegalStateException("Unknown enum: " + remove.toString());
    }
  }
}

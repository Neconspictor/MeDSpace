package controllers.response;

import de.unipassau.medspace.register.common.Datasource;
import de.unipassau.medspace.register.Register;

/**
 * A response message for the {@link Register#addDatasource(Datasource)} service.
 */
public class AddResponse extends Response {

  /**
   * Creates a new {@link AddResponse}
   * @param success true if the add service call was successful.
   */
  public AddResponse(boolean success) {
    if (success) {
      result = true;
      response = "Successfully added a new datasource or updated an existing one.";
    } else {
      result = false;
      response = "The datasource couldn't be updated as there exists a newer version of it.";
    }
  }
}
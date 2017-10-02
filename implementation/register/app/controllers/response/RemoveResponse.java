package controllers.response;

import de.unipassau.medspace.register.Register;
import de.unipassau.medspace.register.common.Datasource;

/**
 * A response message for the {@link Register#removeDatasource(Datasource)} service.
 */
public class RemoveResponse extends Response {

  /**
   * Creates a new {@link RemoveResponse}
   * @param success The result of a {@link Register#removeDatasource(Datasource)} service call.
   */
  public RemoveResponse(boolean success) {
    if (success) {
      result = true;
      response = "Successfully removed the datasource.";
    } else {
      result = false;
      response = "Datasource not found.";
    }
  }
}

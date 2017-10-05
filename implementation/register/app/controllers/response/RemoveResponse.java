package controllers.response;

import de.unipassau.medspace.common.message.Response;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.register.Register;

/**
 * A response message for the {@link Register#removeDatasource(Datasource)} service.
 */
public class RemoveResponse extends Response {

  /**
   * Creates a new {@link RemoveResponse}
   * @param success The result of a {@link Register#removeDatasource(Datasource)} service call.
   */
  public RemoveResponse(boolean success) {
    this.success = success;
    if (success) {
      message = "Successfully removed the datasource.";
    } else {
      message = "Datasource not found.";
    }
  }
}

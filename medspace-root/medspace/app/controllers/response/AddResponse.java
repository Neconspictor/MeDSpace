package controllers.response;

import de.unipassau.medspace.common.message.Response;
import de.unipassau.medspace.common.register.Datasource;
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

    this.success = success;
    if (this.success) {
      message = "Successfully added a new datasource or updated an existing one.";
    } else {
      message = "The datasource couldn't be updated as there exists a newer version of it.";
    }
  }
}
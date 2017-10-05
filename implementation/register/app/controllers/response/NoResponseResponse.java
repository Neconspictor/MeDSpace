package controllers.response;

import de.unipassau.medspace.common.message.Response;
import de.unipassau.medspace.common.register.Datasource;
import de.unipassau.medspace.register.Register;
import de.unipassau.medspace.register.Register.NoResponse;

/**
 * A response message for the {@link Register#datasourceNoRespond(Datasource)} service.
 */
public class NoResponseResponse extends Response {

  /**
   * Creates a new {@link NoResponseResponse}
   * @param noResponse The result of a {@link Register#datasourceNoRespond(Datasource)} call.
   */
  public NoResponseResponse(NoResponse noResponse) {
    switch(noResponse) {
      case REMOVED_DATASOURCE:
        success = true;
        message = "Removed not responding datasource.";
        break;
      case DATASOURCE_NOT_FOUND:
        success = false;
        message = "Datasource isn't registered, thus nothing done.";
        break;
      case COOL_DOWN_ACTIVE:
        success = false;
        message = "Datasource wasn't removed. Its cooldown is still active.";
        break;
      default:
        throw new IllegalStateException("Unknown enum: " + noResponse.toString());
    }
  }
}
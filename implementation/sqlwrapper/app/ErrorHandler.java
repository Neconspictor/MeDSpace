import play.http.HttpErrorHandler;
import play.mvc.*;
import play.mvc.Http.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Singleton;


/**
 * Hanldes HTTP errors. This class is automatically created by Play.
 */
@Singleton
public class ErrorHandler implements HttpErrorHandler {

  /**
   * Returns an error message to the client when an error occurs regarding the client.
   * @param request The request header.
   * @param statusCode The status code to send
   * @param message The message to sent
   * @return An http response.
   */
  public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
    return CompletableFuture.completedFuture(
        Results.status(statusCode, "A client error occurred: " + message)
    );
  }

  /**
   * Returns an error message to the client when an internal server error occurs.
   * @param request The request header.
   * @param exception The status code to send
   * @return An http response.
   */
  public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
    return CompletableFuture.completedFuture(
        Results.internalServerError("A server error occurred: " + exception.getMessage())
    );
  }
}
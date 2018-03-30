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


  @Override
  public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
    return CompletableFuture.completedFuture(
        Results.status(statusCode, "A client error occurred: " + message)
    );
  }

  @Override
  public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
    return CompletableFuture.completedFuture(
        Results.internalServerError("A server error occurred: " + exception.getMessage())
    );
  }
}
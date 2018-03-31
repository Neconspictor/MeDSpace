import de.unipassau.medspace.common.play.ResourceProvider;
import play.Application;
import play.Environment;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;

/**
 * Created by David Goeth on 3/31/2018.
 */
public class Test {

  @Inject
  public Test(ResourceProvider resourceProvider) {

    File test = resourceProvider.getResourceAsFile("/medspace/medspace-global-server-config.xml");
    System.out.println("File exists: " + test.exists());
    System.out.println("absolute file path: " + test.getAbsolutePath());
  }
}

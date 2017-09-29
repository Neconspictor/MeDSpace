package de.unipassau.medspace.register;

/**
 * Created by David Goeth on 29.09.2017.
 */
public class Results {

  public enum Add {
    NULL_NOT_VALID("NULL_NOT_VALID"),
    SUCCESS("SUCCESS"),
    NO_SUCCESS_NEWER_VERSION_EXISTS("NO_SUCCESS_NEWER_VERSION_EXISTS");

    private final String name;

    /**
     * Createa a new Add enum.
     * @param name The string equivalent of this enum.
     */
    Add(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return  name;
    }
  }

  /**
   * Created by David Goeth on 29.09.2017.
   */
  public enum NoResponse {
    NULL_NOT_VALID("NULL_NOT_VALID"),
    REMOVED_DATASOURCE("REMOVED_DATASOURCE"),
    COOL_DOWN_ACTIVE("COOL_DOWN_ACTIVE"),
    DATASOURCE_NOT_FOUND("DATASOURCE_NOT_FOUND");

    private final String name;

    NoResponse(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return  name;
    }
  }

  public enum Remove {
    NULL_NOT_VALID("NULL_NOT_VALID"),
    SUCCESS("SUCCESS"),
    DATASOURCE_NOT_FOUND("DATASOURCE_NOT_FOUND");

    private final String name;

    Remove(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return  name;
    }
  }
}
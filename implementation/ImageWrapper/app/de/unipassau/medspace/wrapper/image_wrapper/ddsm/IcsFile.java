package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO
 */
public class IcsFile {


  /**
   * TODO
   */
  public static final String DATE_OF_STUDY = "DATE_OF_STUDY";

  /**
   * TODO
   */
  public static final String PATIENT_AGE = "PATIENT_AGE";

  /**
   * TODO
   */
  public static final String DENSITY = "DENSITY";

  /**
   * TODO
   */
  public static final String DATE_DIGITIZED = "DATE_DIGITIZED";

  /**
   * TODO
   */
  public static final String DIGITIZER = "DIGITIZER";

  /**
   * TODO
   */
  public static final String LEFT_CC = "LEFT_CC";

  /**
   * TODO
   */
  public static final String LEFT_MLO = "LEFT_MLO";


  /**
   * TODO
   */
  public static final String RIGHT_CC = "RIGHT_CC";


  /**
   * TODO
   */
  public static final String RIGHT_MLO = "RIGHT_MLO";


  /**
   * TODO
   */
  private Date dateOfStudy;

  /**
   * TODO
   */
  private int patientAge;

  /**
   * TODO
   */
  private int density;

  /**
   * TODO
   */
  private Date dateDigitized;

  /**
   * TODO
   */
  private String digitizer;


  /**
   * TODO
   */
  private DDSM_Image leftCC;

  /**
   * TODO
   */
  private DDSM_Image leftMLO;

  /**
   * TODO
   */
  private DDSM_Image rightCC;

  /**
   * TODO
   */
  private DDSM_Image rightMLO;

  /**
   * TODO
   */
  private String id;


  /**
   * TODO
   */
  private IcsFile() {}

  /**
   * TODO
   * @param file
   * @return
   * @throws IOException
   */
  public static IcsFile parse(File file,
                              String id,
                              DDSM_Image leftCC,
                              DDSM_Image leftMLO,
                              DDSM_Image rightCC,
                              DDSM_Image rightMLO) throws IOException {

    IcsFile result =  Parser.parse(file);
    result.id = id;
    result.leftCC = leftCC;
    result.leftMLO = leftMLO;
    result.rightCC = rightCC;
    result.rightMLO = rightMLO;

    return result;
  }

  /**
   * TODO
   */
  public Date getDateOfStudy() {
    return dateOfStudy;
  }

  /**
   * TODO
   */
  public int getPatientAge() {
    return patientAge;
  }

  /**
   * TODO
   */
  public int getDensity() {
    return density;
  }

  /**
   * TODO
   */
  public Date getDateDigitized() {
    return dateDigitized;
  }

  /**
   * TODO
   */
  public String getDigitizer() {
    return digitizer;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy");

    builder.append("IcsFile: {\n");
    builder.append("dateOfStudy: " + formater.format(dateOfStudy) + "\n");
    builder.append("patientAge: " + patientAge + "\n");
    builder.append("density: " + density + "\n");
    builder.append("dateDigitized: " + formater.format(dateDigitized) + "\n");
    builder.append("digitizer: " + digitizer + "\n");

    builder.append("leftCC: " + leftCC + "\n");
    builder.append("leftMLO: " + leftMLO + "\n");
    builder.append("rightCC: " + rightCC + "\n");
    builder.append("rightMLO: " + rightMLO + "\n");
    builder.append("}");

    return builder.toString();
  }

  /**
   * TODO
   */
  public DDSM_Image getLeftCC() {
    return leftCC;
  }

  /**
   * TODO
   */
  public DDSM_Image getLeftMLO() {
    return leftMLO;
  }

  /**
   * TODO
   */
  public DDSM_Image getRightCC() {
    return rightCC;
  }

  /**
   * TODO
   */
  public DDSM_Image getRightMLO() {
    return rightMLO;
  }

  /**
   * TODO
   */
  public String getId() {
    return id;
  }


  /**
   * TODO
   */
  private static class Parser{

    /**
     * TODO
     * @param file
     * @return
     * @throws IOException
     */
    public static IcsFile parse(File file) throws IOException {
      IcsFile data = createUninitialized();
      List<String> content = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.getLineContent(file);
      for (String line : content) {
        List<String> tokens = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.tokenize(line);
        setField(tokens, data);
      }

      //check that all necessary fields are set
      if (!check(data)) {
        throw new IOException("Couldn't parse all relevant field of the date: " + data);
      }

      return data;
    }

    /**
     * TODO
     * @return
     */
    private static IcsFile createUninitialized() {
      IcsFile data = new IcsFile();
      data.dateOfStudy = null;
      data.patientAge  = -1;
      data.density = -1;
      data.dateDigitized = null;
      data.digitizer = null;
      return data;
    }

    /**
     * TODO
     * @param data
     * @return
     */
    private static boolean check(IcsFile data) {
      return data.digitizer != null &&
          data.dateDigitized != null &&
          data.density >= 0 &&
          data.patientAge >= 0 &&
          data.dateOfStudy != null;
    }

    /**
     * TODO
     * @param tokens
     * @param data
     * @throws IOException
     */
    private static void setField(List<String> tokens, IcsFile data) throws IOException {

      //at least we need two tokens (key-value pair)
      if (tokens.size() < 2) return;
      String key = tokens.get(0);
      tokens.remove(0);

      switch(key) {
        case DATE_OF_STUDY:
          data.dateOfStudy = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.parseDateField(tokens);
          break;
        case PATIENT_AGE:
          data.patientAge = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.parseInt(tokens);
          break;
        case DENSITY:
          data.density = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.parseInt(tokens);
          break;
        case DATE_DIGITIZED:
          data.dateDigitized = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.parseDateField(tokens);
          break;
        case DIGITIZER:
          data.digitizer = de.unipassau.medspace.wrapper.image_wrapper.ddsm.Util.parseText(tokens);
          break;
        default:
          break;
      }
    }
  }
}
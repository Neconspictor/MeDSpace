package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.ParserUtil;
import de.unipassau.medspace.common.util.StringUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents an ICS file.
 */
public class IcsFile extends DDSM_CaseFile {


  /**
   * The DATE_OF_STUDY token in an ICS file.
   */
  public static final String DATE_OF_STUDY = "DATE_OF_STUDY";

  /**
   * The PATIENT_AGE token in an ICS file.
   */
  public static final String PATIENT_AGE = "PATIENT_AGE";

  /**
   * The DENSITY token in an ICS file.
   */
  public static final String DENSITY = "DENSITY";

  /**
   * The DATE_DIGITIZED token in an ICS file.
   */
  public static final String DATE_DIGITIZED = "DATE_DIGITIZED";

  /**
   * The DIGITIZER token in an ICS file.
   */
  public static final String DIGITIZER = "DIGITIZER";

  /**
   * The LEFT_CC token in an ICS file.
   */
  public static final String LEFT_CC = "LEFT_CC";

  /**
   * The LEFT_MLO token in an ICS file.
   */
  public static final String LEFT_MLO = "LEFT_MLO";

  /**
   * The RIGHT_CC token in an ICS file.
   */
  public static final String RIGHT_CC = "RIGHT_CC";

  /**
   * The RIGHT_MLO token in an ICS file.
   */
  public static final String RIGHT_MLO = "RIGHT_MLO";



  private Date dateOfStudy;

  private int patientAge;

  private int density;

  private Date dateDigitized;

  private String digitizer;

  private Image leftCC;

  private Image leftMLO;

  private Image rightCC;

  private Image rightMLO;


  /**
   * Creates a new IcsFile object.
   *
   * @param id the ID of this ICS file.
   * @param caseName The name of the case this objects belongs to.
   * @param source The source ICS file.
   */
  private IcsFile(String id,
                  String caseName,
                  File source) {
    super(id, caseName, source);
  }

  /**
   * Parses an IcsFile from a given file.
   * @param file  The source ICS file.
   * @param id the ID of this ICS file.
   * @param caseName The name of the case this lesion belongs to.
   * @param leftCC The leftCC image
   * @param leftMLO The leftMLO image
   * @param rightCC The rightCC image
   * @param rightMLO The rightMLO image
   * @return an IcsFile from a given file.
   *
   * @throws IOException If an IO error occurs.
   */
  public static IcsFile parse(File file,
                              String id,
                              String caseName,
                              Image leftCC,
                              Image leftMLO,
                              Image rightCC,
                              Image rightMLO) throws IOException {

    IcsFile result =  Parser.parse(file, id, caseName);
    result.leftCC = leftCC;
    result.leftMLO = leftMLO;
    result.rightCC = rightCC;
    result.rightMLO = rightMLO;

    return result;
  }


  /**
   * Provides the date of study.
   * @return the date of study.
   */
  public Date getDateOfStudy() {
    return dateOfStudy;
  }

  /**
   * Provides the age of the patient.
   * @return the age of the patient.
   */
  public int getPatientAge() {
    return patientAge;
  }

  /**
   * Provides the density
   * @return the density
   */
  public int getDensity() {
    return density;
  }

  /**
   * Provides the date the images were digitized.
   * @return the date the images were digitized.
   */
  public Date getDateDigitized() {
    return dateDigitized;
  }

  /**
   * Provides the name of the used digitizer.
   * @return the name of the used digitizer.
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
   * Provides the leftCC image.
   * @return the leftCC image.
   */
  public Image getLeftCC() {
    return leftCC;
  }

  /**
   * Provides the leftMLO image
   * @return the leftMLO image.
   */
  public Image getLeftMLO() {
    return leftMLO;
  }

  /**
   * Provides the rightCC image.
   * @return the rightCC image.
   */
  public Image getRightCC() {
    return rightCC;
  }

  /**
   * Provides the rightMLO image.
   * @return the rightMLO image.
   */
  public Image getRightMLO() {
    return rightMLO;
  }



  /**
   * A parser for an ICSFile
   */
  private static class Parser {

    /**
     * Parses an ICS file
     * @param file The source ICS file
     * @param id The id for the ICS file
     * @param caseName The name of the DDSM case the ICS file belongs to.
     * @return The parsed IcsFile
     * @throws IOException If an IO error occurs.
     */
    public static IcsFile parse(File file, String id, String caseName) throws IOException {
      IcsFile data = createUninitialized(id, caseName, file);
      List<String> content = FileUtil.getLineContent(file);
      for (String line : content) {
        List<String> tokens = StringUtil.tokenize(line, " \t");
        setField(tokens, data);
      }

      //check that all necessary fields are set
      if (!check(data)) {
        throw new IOException("Couldn't parse all relevant field of the date: " + data);
      }

      return data;
    }

    private static IcsFile createUninitialized(String id, String caseName, File source) {
      IcsFile data = new IcsFile(id, caseName, source);
      data.dateOfStudy = null;
      data.patientAge  = -1;
      data.density = -1;
      data.dateDigitized = null;
      data.digitizer = null;
      return data;
    }


    private static boolean check(IcsFile data) {
      return data.digitizer != null &&
          data.dateDigitized != null &&
          data.density >= 0 &&
          data.patientAge >= 0 &&
          data.dateOfStudy != null;
    }


    private static void setField(List<String> tokens, IcsFile data) throws IOException {

      //at least we need two tokens (key-value pair)
      if (tokens.size() < 2) return;
      String key = tokens.get(0);
      tokens.remove(0);

      switch(key) {
        case DATE_OF_STUDY:
          data.dateOfStudy = ParserUtil.pullDateField(tokens);
          break;
        case PATIENT_AGE:
          data.patientAge = ParserUtil.pullInt(tokens);
          break;
        case DENSITY:
          data.density = ParserUtil.pullInt(tokens);
          break;
        case DATE_DIGITIZED:
          data.dateDigitized = ParserUtil.pullDateField(tokens);
          break;
        case DIGITIZER:
          data.digitizer = StringUtil.concat(tokens, " ");
          break;
        default:
          break;
      }
    }
  }
}
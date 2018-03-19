package de.unipassau.medspace.wrapper.image_wrapper.ddsm;

import de.unipassau.medspace.common.util.FileUtil;
import de.unipassau.medspace.common.util.ParserUtil;
import de.unipassau.medspace.common.util.StringUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO
 */
public class IcsFile extends DDSM_CaseFile {


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

  public static final List<String> FIELD_META_DATA = Arrays.asList(DATE_OF_STUDY, PATIENT_AGE, DENSITY, DATE_DIGITIZED,
      DIGITIZER, LEFT_CC, LEFT_MLO, RIGHT_CC, "ICS FILE", "DDSM");


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
  private Image leftCC;

  /**
   * TODO
   */
  private Image leftMLO;

  /**
   * TODO
   */
  private Image rightCC;

  /**
   * TODO
   */
  private Image rightMLO;


  /**
   * TODO
   */
  private IcsFile(String id,
                  String caseName,
                  File source) {
    super(id, caseName, source);
  }

  /**
   * TODO
   * @param file
   * @return
   * @throws IOException
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
  public Image getLeftCC() {
    return leftCC;
  }

  /**
   * TODO
   */
  public Image getLeftMLO() {
    return leftMLO;
  }

  /**
   * TODO
   */
  public Image getRightCC() {
    return rightCC;
  }

  /**
   * TODO
   */
  public Image getRightMLO() {
    return rightMLO;
  }

  /**
   * TODO
   */
  public File getSource() {
    return source;
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

    /**
     * TODO
     * @return
     */
    private static IcsFile createUninitialized(String id, String caseName, File source) {
      IcsFile data = new IcsFile(id, caseName, source);
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
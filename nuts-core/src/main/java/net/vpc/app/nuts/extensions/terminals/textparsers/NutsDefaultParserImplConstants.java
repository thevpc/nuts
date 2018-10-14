/* Generated By:JavaCC: Do not edit this line. NutsDefaultParserImplConstants.java */
package net.vpc.app.nuts.extensions.terminals.textparsers;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface NutsDefaultParserImplConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int BRAKS_S = 1;
  /** RegularExpression Id. */
  int BRAKS_E = 2;
  /** RegularExpression Id. */
  int SP4_S = 3;
  /** RegularExpression Id. */
  int SP4_E = 4;
  /** RegularExpression Id. */
  int SP3_S = 5;
  /** RegularExpression Id. */
  int SP3_E = 6;
  /** RegularExpression Id. */
  int SP2_S = 7;
  /** RegularExpression Id. */
  int SP2_E = 8;
  /** RegularExpression Id. */
  int PAR_S = 9;
  /** RegularExpression Id. */
  int PAR_E = 10;
  /** RegularExpression Id. */
  int ACC_S = 11;
  /** RegularExpression Id. */
  int ACC_E = 12;
  /** RegularExpression Id. */
  int EQ4 = 13;
  /** RegularExpression Id. */
  int EQ3 = 14;
  /** RegularExpression Id. */
  int EQ2 = 15;
  /** RegularExpression Id. */
  int STAR4 = 16;
  /** RegularExpression Id. */
  int STAR3 = 17;
  /** RegularExpression Id. */
  int STAR2 = 18;
  /** RegularExpression Id. */
  int HAT4 = 19;
  /** RegularExpression Id. */
  int HAT3 = 20;
  /** RegularExpression Id. */
  int HAT2 = 21;
  /** RegularExpression Id. */
  int AT4 = 22;
  /** RegularExpression Id. */
  int AT3 = 23;
  /** RegularExpression Id. */
  int AT2 = 24;
  /** RegularExpression Id. */
  int DOLLAR4 = 25;
  /** RegularExpression Id. */
  int DOLLAR3 = 26;
  /** RegularExpression Id. */
  int DOLLAR2 = 27;
  /** RegularExpression Id. */
  int POUND4 = 28;
  /** RegularExpression Id. */
  int POUND3 = 29;
  /** RegularExpression Id. */
  int POUND2 = 30;
  /** RegularExpression Id. */
  int TILDE4 = 31;
  /** RegularExpression Id. */
  int TILDE3 = 32;
  /** RegularExpression Id. */
  int TILDE2 = 33;
  /** RegularExpression Id. */
  int DIV4 = 34;
  /** RegularExpression Id. */
  int DIV3 = 35;
  /** RegularExpression Id. */
  int DIV2 = 36;
  /** RegularExpression Id. */
  int PLUS4 = 37;
  /** RegularExpression Id. */
  int PLUS3 = 38;
  /** RegularExpression Id. */
  int PLUS2 = 39;
  /** RegularExpression Id. */
  int PERCENT4 = 40;
  /** RegularExpression Id. */
  int PERCENT3 = 41;
  /** RegularExpression Id. */
  int PERCENT2 = 42;
  /** RegularExpression Id. */
  int SHARP4 = 43;
  /** RegularExpression Id. */
  int SHARP3 = 44;
  /** RegularExpression Id. */
  int SHARP2 = 45;
  /** RegularExpression Id. */
  int PLANET4 = 46;
  /** RegularExpression Id. */
  int PLANET3 = 47;
  /** RegularExpression Id. */
  int PLANET2 = 48;
  /** RegularExpression Id. */
  int PGH4 = 49;
  /** RegularExpression Id. */
  int PGH3 = 50;
  /** RegularExpression Id. */
  int PGH2 = 51;
  /** RegularExpression Id. */
  int TREMA4 = 52;
  /** RegularExpression Id. */
  int TREMA3 = 53;
  /** RegularExpression Id. */
  int TREMA2 = 54;
  /** RegularExpression Id. */
  int DQUOTE3 = 55;
  /** RegularExpression Id. */
  int DQUOTE2 = 56;
  /** RegularExpression Id. */
  int DQUOTE1 = 57;
  /** RegularExpression Id. */
  int SQUOTE3 = 58;
  /** RegularExpression Id. */
  int SQUOTE2 = 59;
  /** RegularExpression Id. */
  int SQUOTE1 = 60;
  /** RegularExpression Id. */
  int ANTI_CHAR4 = 61;
  /** RegularExpression Id. */
  int ANTI_CHAR3 = 62;
  /** RegularExpression Id. */
  int ANTI_CHAR2 = 63;
  /** RegularExpression Id. */
  int ANTI_CHAR = 64;
  /** RegularExpression Id. */
  int PHRASE = 65;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\"[\"",
    "\"]\"",
    "\"<<<<\"",
    "\">>>>\"",
    "\"<<<\"",
    "\">>>\"",
    "\"<<\"",
    "\">>\"",
    "\"(\"",
    "\")\"",
    "\"{\"",
    "\"}\"",
    "\"====\"",
    "\"===\"",
    "\"==\"",
    "\"****\"",
    "\"***\"",
    "\"**\"",
    "\"^^^^\"",
    "\"^^^\"",
    "\"^^\"",
    "\"@@@@\"",
    "\"@@@\"",
    "\"@@\"",
    "\"$$$$\"",
    "\"$$$\"",
    "\"$$\"",
    "\"\\u00a3\\u00a3\\u00a3\\u00a3\"",
    "\"\\u00a3\\u00a3\\u00a3\"",
    "\"\\u00a3\\u00a3\"",
    "\"~~~~\"",
    "\"~~~\"",
    "\"~~\"",
    "\"////\"",
    "\"///\"",
    "\"//\"",
    "\"++++\"",
    "\"+++\"",
    "\"++\"",
    "\"%%%%\"",
    "\"%%%\"",
    "\"%%\"",
    "\"####\"",
    "\"###\"",
    "\"##\"",
    "\"\\u00a4\\u00a4\\u00a4\\u00a4\"",
    "\"\\u00a4\\u00a4\\u00a4\"",
    "\"\\u00a4\\u00a4\"",
    "\"\\u00a7\\u00a7\\u00a7\\u00a7\"",
    "\"\\u00a7\\u00a7\\u00a7\"",
    "\"\\u00a7\\u00a7\"",
    "\"\\u00a8\\u00a8\\u00a8\\u00a8\"",
    "\"\\u00a8\\u00a8\\u00a8\"",
    "\"\\u00a8\\u00a8\"",
    "\"\\\"\\\"\\\"\"",
    "\"\\\"\\\"\"",
    "\"\\\"\"",
    "\"\\\'\\\'\\\'\"",
    "\"\\\'\\\'\"",
    "\"\\\'\"",
    "<ANTI_CHAR4>",
    "<ANTI_CHAR3>",
    "<ANTI_CHAR2>",
    "<ANTI_CHAR>",
    "<PHRASE>",
  };

}
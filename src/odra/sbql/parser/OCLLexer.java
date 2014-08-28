/* The following code was generated by JFlex 1.3.5 on 8/28/14 3:10 PM */

package odra.sbql.parser;

import odra.exceptions.*;
import odra.sbql.ast.ParserException;
import java.util.Date;
import odra.util.DateUtils;



/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.3.5
 * on 8/28/14 3:10 PM from the specification file
 * <tt>file:/Users/greg/Documents/School/Science_Research/workspace/ODRA-with-Enums/res/ocl-skaner.jflex</tt>
 */
public class OCLLexer implements java_cup.runtime.Scanner {

  /** This character denotes the end of file */
  final public static int YYEOF = -1;

  /** initial size of the lookahead buffer */
  final private static int YY_BUFFERSIZE = 16384;

  /** lexical states */
  final public static int STRING = 1;
  final public static int BLOCKCOMMENT = 3;
  final public static int YYINITIAL = 0;
  final public static int LINECOMMENT = 2;

  /** 
   * Translates characters to character classes
   */
  final private static String yycmap_packed = 
    "\11\0\1\16\1\20\1\0\1\16\1\17\22\0\1\10\6\0\1\73"+
    "\1\23\1\24\1\22\1\34\1\32\1\1\1\3\1\21\1\4\1\5"+
    "\1\6\1\7\2\12\4\2\1\11\1\31\1\36\1\35\1\37\1\0"+
    "\1\13\1\14\1\64\14\14\1\67\3\14\1\63\1\70\6\14\1\27"+
    "\1\15\1\30\1\0\1\14\1\0\1\53\1\72\1\52\1\50\1\44"+
    "\1\41\1\65\1\43\1\40\1\14\1\61\1\46\1\62\1\45\1\55"+
    "\1\60\1\66\1\56\1\47\1\42\1\54\1\14\1\51\1\71\1\57"+
    "\1\14\1\25\1\33\1\26\uff82\0";

  /** 
   * Translates characters to character classes
   */
  final private static char [] yycmap = yy_unpack_cmap(yycmap_packed);

  /** 
   * Translates a state to a row index in the transition table
   */
  final private static int yy_rowMap [] = { 
        0,    60,   120,   180,   240,   300,   360,   420,   240,   480, 
      540,   600,   660,   720,   240,   240,   240,   240,   240,   240, 
      240,   240,   240,   780,   240,   840,   900,   960,  1020,  1080, 
     1140,  1200,  1260,  1320,  1380,  1440,  1500,  1560,  1620,  1680, 
     1740,  1800,  1860,  1920,   240,   240,   240,  1980,   240,  2040, 
     2100,  2160,   240,   240,  2220,  2160,   240,   240,   240,   240, 
      240,   240,   240,   240,   240,   240,   240,   540,  2280,  2340, 
     2400,  2460,  2520,   540,  2580,  2640,  2700,  2760,  2820,  2880, 
     2940,  3000,   540,  3060,  3120,  3180,  3240,  3300,  3360,  3420, 
     3480,  3540,  3600,  3660,  3720,  3780,  3840,  3900,  3960,  4020, 
     4080,  4140,  4200,   540,  4260,  4320,   540,  4380,   540,  4440, 
     4500,  4560,  4620,  4680,  4740,  4800,  4860,  4920,  4980,  5040, 
     5100,   540,  5160,  5220,  5280,  5340,  5400,  5460,  5520,  5580, 
      540,   540,  5640,   540,  5700,   540,   540,  5760,  5820,  5880, 
      540,  5940,  6000,   540,  6060,  6120,  6180,  6240,  6300,  6360, 
     6420,  6480,   540,  6540,  6600,   540,  6660,   540,   540,  6720, 
     6780,  6840,   540,   540,  6900,  6960,  7020,  7080,  7140,  7200, 
     7260,  7320,  7380,  7440,   540,  7500,   540,  7560,   540,  7620, 
      540,   540,   540,  7680,  7740,  7800,  7860,  7920,   540,   540, 
      540,   540,  7980,  8040,  8100,  8160,  8220,  8280,  8340,  8400, 
     8460,  8520,   540,  8580,  8640,  8700,  8760,  8820,  8880,  8940, 
     9000,  9060,  9120,  9180,  9240,  9300,  9360,   240
  };

  /** 
   * The packed transition table of the DFA (part 0)
   */
  final private static String yy_packed0 = 
    "\1\5\1\6\1\7\1\10\4\7\1\11\1\12\1\7"+
    "\2\13\1\5\1\11\1\14\1\11\1\15\1\16\1\17"+
    "\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27"+
    "\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\13"+
    "\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\13"+
    "\1\46\1\13\1\47\4\13\1\50\1\51\2\13\1\52"+
    "\1\53\1\13\1\54\1\55\15\56\1\0\1\56\2\0"+
    "\52\56\1\57\17\11\1\60\1\61\72\11\1\14\2\11"+
    "\1\62\51\11\76\0\1\63\1\64\4\63\2\0\1\63"+
    "\22\0\1\65\1\0\1\66\36\0\1\67\1\64\4\67"+
    "\2\0\1\67\63\0\1\70\1\71\4\70\2\0\1\70"+
    "\72\0\1\72\23\0\1\73\40\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\33\13\21\0\1\11"+
    "\74\0\1\74\1\75\12\0\1\76\73\0\1\77\73\0"+
    "\1\100\73\0\1\101\1\0\1\102\71\0\1\103\40\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\1\13\1\104\3\13\1\105\25\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\13\13\1\106"+
    "\1\13\1\107\1\110\14\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\3\13\1\111\11\13"+
    "\1\112\1\113\14\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\5\13\1\114\1\115\24\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\15\13\1\116\15\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\1\117\3\13\1\120"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\11\13\1\121\21\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\122"+
    "\10\13\1\123\15\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\1\124\2\13\1\125\27\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\13\13\1\126\1\13\1\127\1\130\14\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\5\13\1\131\25\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\4\13\1\132\26\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\4\13\1\133\26\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\13\13\1\134\17\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\16\13\1\135\14\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\14\13\1\136\16\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\15\13\1\137\15\13\21\0\1\61\74\0\1\61\54\0"+
    "\1\63\1\64\4\63\2\0\1\63\63\0\1\70\1\0"+
    "\4\70\2\0\1\70\63\0\1\140\1\64\4\140\2\0"+
    "\1\140\63\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\7\13\1\141\23\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\6\13\1\142"+
    "\24\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\16\13\1\143\14\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\15\13\1\144"+
    "\15\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\4\13\1\145\11\13\1\146\14\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\14\13\1\147\2\13\1\150\13\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\10\13\1\151"+
    "\22\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\7\13\1\152\23\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\20\13\1\153"+
    "\12\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\5\13\1\154\25\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\2\13\1\155"+
    "\30\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\1\156\32\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\1\13\1\157\4\13"+
    "\1\160\24\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\2\13\1\161\30\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\1\162"+
    "\32\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\2\13\1\163\4\13\1\164\23\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\5\13\1\165\25\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\4\13\1\166\26\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\6\13\1\167\24\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\2\13\1\170\15\13\1\171"+
    "\12\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\2\13\1\172\23\13\1\173\4\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\25\13\1\172\5\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\10\13\1\174\22\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\20\13\1\175\12\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\10\13\1\176\22\13\3\0"+
    "\1\177\1\64\4\177\2\0\1\177\63\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\200"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\7\13\1\201\23\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\202"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\22\13\1\203\10\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\5\13\1\204"+
    "\25\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\15\13\1\205\15\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\206"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\1\207\32\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\4\13\1\210\26\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\21\13\1\211\11\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\2\13\1\212\30\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\13\13\1\213\17\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\4\13\1\214\26\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\3\13\1\215\27\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\6\13\1\216\24\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\12\13\1\217\20\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\4\13\1\220\26\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\2\13\1\221\30\13\3\0\1\13\1\0\4\13"+
    "\2\0\1\13\1\0\1\13\23\0\13\13\1\222\17\13"+
    "\3\0\1\13\1\0\4\13\2\0\1\13\1\0\1\13"+
    "\23\0\1\223\32\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\14\13\1\224\16\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\6\13\1\225\24\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\14\13\1\226\16\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\4\13\1\227\26\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\6\13\1\230\24\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\17\13\1\231\13\13\2\0\1\232\1\63\1\64\4\63"+
    "\2\0\1\63\63\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\16\13\1\233\14\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\4\13"+
    "\1\234\26\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\13\13\1\235\17\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\11\13"+
    "\1\236\21\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\1\13\1\237\31\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\12\13"+
    "\1\240\20\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\14\13\1\241\16\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\2\13"+
    "\1\242\30\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\4\13\1\243\26\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\3\13"+
    "\1\244\27\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\4\13\1\245\26\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\2\13"+
    "\1\246\30\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\5\13\1\247\25\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\16\13"+
    "\1\250\14\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\13\13\1\251\17\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\4\13"+
    "\1\252\26\13\3\0\1\13\1\0\4\13\2\0\1\13"+
    "\1\0\1\13\23\0\16\13\1\253\14\13\3\0\1\13"+
    "\1\0\4\13\2\0\1\13\1\0\1\13\23\0\4\13"+
    "\1\254\26\13\5\0\1\255\1\256\70\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\2\13\1\257"+
    "\30\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\12\13\1\260\20\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\3\13\1\261"+
    "\27\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\6\13\1\262\24\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\263"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\31\13\1\264\1\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\265"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\21\13\1\266\11\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\5\13\1\267"+
    "\25\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\12\13\1\270\20\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\5\13\1\271"+
    "\25\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\4\13\1\272\26\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\30\13\1\273"+
    "\2\13\3\0\1\274\2\0\3\274\2\0\1\274\65\0"+
    "\3\274\67\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\3\13\1\275\27\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\2\13\1\276"+
    "\30\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\2\13\1\277\30\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\4\13\1\300"+
    "\26\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\12\13\1\301\20\13\3\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\10\13\1\302"+
    "\22\13\3\0\1\13\1\0\4\13\2\0\1\13\1\0"+
    "\1\13\23\0\17\13\1\303\13\13\2\0\1\304\74\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\4\13\1\172\26\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\23\13\1\305\7\13\3\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\20\13\1\306\12\13\5\0\1\307\2\310\1\311\66\0"+
    "\1\13\1\0\4\13\2\0\1\13\1\0\1\13\23\0"+
    "\4\13\1\312\26\13\3\0\1\13\1\0\4\13\2\0"+
    "\1\13\1\0\1\13\23\0\4\13\1\313\26\13\3\0"+
    "\1\314\2\0\3\314\2\0\1\314\63\0\1\314\1\0"+
    "\4\314\2\0\1\314\65\0\2\314\70\0\1\13\1\0"+
    "\4\13\2\0\1\13\1\0\1\13\23\0\2\13\1\172"+
    "\30\13\11\0\1\315\67\0\2\316\1\317\67\0\1\320"+
    "\1\0\4\320\2\0\1\320\65\0\4\320\75\0\1\321"+
    "\66\0\4\322\2\0\1\322\63\0\1\323\1\0\4\323"+
    "\2\0\1\323\72\0\1\324\66\0\4\325\2\0\1\325"+
    "\63\0\1\326\1\0\4\326\2\0\1\326\64\0\1\327"+
    "\72\0\1\330\1\0\4\330\2\0\1\330\63\0\1\331"+
    "\1\0\4\331\2\0\1\331\63\0\1\332\1\0\4\332"+
    "\2\0\1\332\61\0";

  /** 
   * The transition table of the DFA
   */
  final private static int yytrans [] = yy_unpack();


  /* error codes */
  final private static int YY_UNKNOWN_ERROR = 0;
  final private static int YY_ILLEGAL_STATE = 1;
  final private static int YY_NO_MATCH = 2;
  final private static int YY_PUSHBACK_2BIG = 3;

  /* error messages for the codes above */
  final private static String YY_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Internal error: unknown state",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * YY_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private final static byte YY_ATTRIBUTE[] = {
     0,  0,  0,  0,  9,  1,  1,  1,  9,  1,  1,  1,  1,  1,  9,  9, 
     9,  9,  9,  9,  9,  9,  9,  1,  9,  1,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  9,  9,  9,  1, 
     9,  1,  1,  0,  9,  9,  1,  1,  9,  9,  9,  9,  9,  9,  9,  9, 
     9,  9,  9,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  0,  1,  1,  1,  1,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  0,  0,  1,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  0,  1,  1,  1,  1, 
     1,  1,  1,  0,  1,  1,  0,  0,  0,  1,  1,  1,  0,  0,  0,  0, 
     0,  0,  1,  0,  0,  1,  0,  1,  1,  9
  };

  /** the input device */
  private java.io.Reader yy_reader;

  /** the current state of the DFA */
  private int yy_state;

  /** the current lexical state */
  private int yy_lexical_state = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char yy_buffer[] = new char[YY_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int yy_markedPos;

  /** the textposition at the last state to be included in yytext */
  private int yy_pushbackPos;

  /** the current text position in the buffer */
  private int yy_currentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int yy_startRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int yy_endRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn; 

  /** 
   * yy_atBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean yy_atBOL = true;

  /** yy_atEOF == true <=> the scanner is at the EOF */
  private boolean yy_atEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean yy_eof_done;

  /* user code: */
	private StringBuffer str;
	public String module = "";

	public OCLLexer(java.io.Reader in, String mod) {
		this.yy_reader = in;
		module = mod;
	}

	public OCLLexer(java.io.InputStream in, String mod) {	
		this(new java.io.InputStreamReader(in));
		module = mod;
	}


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public OCLLexer(java.io.Reader in) {
    this.yy_reader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public OCLLexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  /** 
   * Unpacks the split, compressed DFA transition table.
   *
   * @return the unpacked transition table
   */
  private static int [] yy_unpack() {
    int [] trans = new int[9420];
    int offset = 0;
    offset = yy_unpack(yy_packed0, offset, trans);
    return trans;
  }

  /** 
   * Unpacks the compressed DFA transition table.
   *
   * @param packed   the packed transition table
   * @return         the index of the last entry
   */
  private static int yy_unpack(String packed, int offset, int [] trans) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do trans[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] yy_unpack_cmap(String packed) {
    char [] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 150) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }


  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   * 
   * @exception   IOException  if any I/O-Error occurs
   */
  private boolean yy_refill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (yy_startRead > 0) {
      System.arraycopy(yy_buffer, yy_startRead, 
                       yy_buffer, 0, 
                       yy_endRead-yy_startRead);

      /* translate stored positions */
      yy_endRead-= yy_startRead;
      yy_currentPos-= yy_startRead;
      yy_markedPos-= yy_startRead;
      yy_pushbackPos-= yy_startRead;
      yy_startRead = 0;
    }

    /* is the buffer big enough? */
    if (yy_currentPos >= yy_buffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[yy_currentPos*2];
      System.arraycopy(yy_buffer, 0, newBuffer, 0, yy_buffer.length);
      yy_buffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = yy_reader.read(yy_buffer, yy_endRead, 
                                            yy_buffer.length-yy_endRead);

    if (numRead < 0) {
      return true;
    }
    else {
      yy_endRead+= numRead;  
      return false;
    }
  }


  /**
   * Closes the input stream.
   */
  final public void yyclose() throws java.io.IOException {
    yy_atEOF = true;            /* indicate end of file */
    yy_endRead = yy_startRead;  /* invalidate buffer    */

    if (yy_reader != null)
      yy_reader.close();
  }


  /**
   * Closes the current stream, and resets the
   * scanner to read from a new input stream.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>YY_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  final public void yyreset(java.io.Reader reader) throws java.io.IOException {
    yyclose();
    yy_reader = reader;
    yy_atBOL  = true;
    yy_atEOF  = false;
    yy_endRead = yy_startRead = 0;
    yy_currentPos = yy_markedPos = yy_pushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    yy_lexical_state = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  final public int yystate() {
    return yy_lexical_state;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  final public void yybegin(int newState) {
    yy_lexical_state = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  final public String yytext() {
    return new String( yy_buffer, yy_startRead, yy_markedPos-yy_startRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  final public char yycharat(int pos) {
    return yy_buffer[yy_startRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  final public int yylength() {
    return yy_markedPos-yy_startRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void yy_ScanError(int errorCode) throws ParserException {
    String message;
    try {
      message = YY_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = YY_ERROR_MSG[YY_UNKNOWN_ERROR];
    }

    throw new ParserException(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  private void yypushback(int number)  throws ParserException {
    if ( number > yylength() )
      yy_ScanError(YY_PUSHBACK_2BIG);

    yy_markedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void yy_do_eof() throws java.io.IOException {
    if (!yy_eof_done) {
      yy_eof_done = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   IOException  if any I/O-Error occurs
   */
  public java_cup.runtime.Symbol next_token() throws java.io.IOException, ParserException {
    int yy_input;
    int yy_action;

    // cached fields:
    int yy_currentPos_l;
    int yy_startRead_l;
    int yy_markedPos_l;
    int yy_endRead_l = yy_endRead;
    char [] yy_buffer_l = yy_buffer;
    char [] yycmap_l = yycmap;

    int [] yytrans_l = yytrans;
    int [] yy_rowMap_l = yy_rowMap;
    byte [] yy_attr_l = YY_ATTRIBUTE;

    while (true) {
      yy_markedPos_l = yy_markedPos;

      boolean yy_r = false;
      for (yy_currentPos_l = yy_startRead; yy_currentPos_l < yy_markedPos_l;
                                                             yy_currentPos_l++) {
        switch (yy_buffer_l[yy_currentPos_l]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          yy_r = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          yy_r = true;
          break;
        case '\n':
          if (yy_r)
            yy_r = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          yy_r = false;
          yycolumn++;
        }
      }

      if (yy_r) {
        // peek one character ahead if it is \n (if we have counted one line too much)
        boolean yy_peek;
        if (yy_markedPos_l < yy_endRead_l)
          yy_peek = yy_buffer_l[yy_markedPos_l] == '\n';
        else if (yy_atEOF)
          yy_peek = false;
        else {
          boolean eof = yy_refill();
          yy_markedPos_l = yy_markedPos;
          yy_buffer_l = yy_buffer;
          if (eof) 
            yy_peek = false;
          else 
            yy_peek = yy_buffer_l[yy_markedPos_l] == '\n';
        }
        if (yy_peek) yyline--;
      }
      yy_action = -1;

      yy_startRead_l = yy_currentPos_l = yy_currentPos = 
                       yy_startRead = yy_markedPos_l;

      yy_state = yy_lexical_state;


      yy_forAction: {
        while (true) {

          if (yy_currentPos_l < yy_endRead_l)
            yy_input = yy_buffer_l[yy_currentPos_l++];
          else if (yy_atEOF) {
            yy_input = YYEOF;
            break yy_forAction;
          }
          else {
            // store back cached positions
            yy_currentPos  = yy_currentPos_l;
            yy_markedPos   = yy_markedPos_l;
            boolean eof = yy_refill();
            // get translated positions and possibly new buffer
            yy_currentPos_l  = yy_currentPos;
            yy_markedPos_l   = yy_markedPos;
            yy_buffer_l      = yy_buffer;
            yy_endRead_l     = yy_endRead;
            if (eof) {
              yy_input = YYEOF;
              break yy_forAction;
            }
            else {
              yy_input = yy_buffer_l[yy_currentPos_l++];
            }
          }
          int yy_next = yytrans_l[ yy_rowMap_l[yy_state] + yycmap_l[yy_input] ];
          if (yy_next == -1) break yy_forAction;
          yy_state = yy_next;

          int yy_attributes = yy_attr_l[yy_state];
          if ( (yy_attributes & 1) == 1 ) {
            yy_action = yy_state; 
            yy_markedPos_l = yy_currentPos_l; 
            if ( (yy_attributes & 8) == 8 ) break yy_forAction;
          }

        }
      }

      // store back cached position
      yy_markedPos = yy_markedPos_l;

      switch (yy_action) {

        case 202: 
          {  return new Token(yyline, yycolumn, sym.TUPLETYPE);  }
        case 219: break;
        case 57: 
          {  return new Token(yyline, yycolumn, sym.DOUBLECOLON);  }
        case 220: break;
        case 56: 
          {  return new Token(yyline, yycolumn, sym.DOUBLEDOT);  }
        case 221: break;
        case 53: 
          {  return new Token(yyline, yycolumn, sym.RIGHTARROW);  }
        case 222: break;
        case 4: 
          {  throw new ParserException("Illegal character '" + yytext() + "'.", module, yyline + 1, yycolumn + 1);  }
        case 223: break;
        case 16: 
          {  return new Token(yyline, yycolumn, sym.LEFTBRACE);  }
        case 224: break;
        case 17: 
          {  return new Token(yyline, yycolumn, sym.RIGHTBRACE);  }
        case 225: break;
        case 18: 
          {  return new Token(yyline, yycolumn, sym.LEFTSQUARE);  }
        case 226: break;
        case 19: 
          {  return new Token(yyline, yycolumn, sym.RIGHTSQUARE);  }
        case 227: break;
        case 20: 
          {  return new Token(yyline, yycolumn, sym.SEMICOLON);  }
        case 228: break;
        case 63: 
          {  return new Token(yyline, yycolumn, sym.ASSIGN_ADD);  }
        case 229: break;
        case 64: 
          {  return new Token(yyline, yycolumn, sym.LOWEREQUALS);  }
        case 230: break;
        case 65: 
          {  return new Token(yyline, yycolumn, sym.DIFFERENT);  }
        case 231: break;
        case 52: 
          {  return new Token(yyline, yycolumn, sym.ASSIGN_SUBTRACT);  }
        case 232: break;
        case 62: 
          {  return new Token(yyline, yycolumn, sym.ASSIGN_MULTIPLY);  }
        case 233: break;
        case 155: 
          {  return new Token(yyline, yycolumn, sym.BooleanLiteralExpCS, new Boolean(false));  }
        case 234: break;
        case 67: 
          {  return new Token(yyline, yycolumn, sym.IF);  }
        case 235: break;
        case 68: 
          {  return new Token(yyline, yycolumn, sym.IN);  }
        case 236: break;
        case 73: 
          {  return new Token(yyline, yycolumn, sym.TO);  }
        case 237: break;
        case 82: 
          {  return new Token(yyline, yycolumn, sym.DO);  }
        case 238: break;
        case 55: 
          {  	
						double val;
						try {
							val = Double.parseDouble(yytext());
						}
						catch (Exception e) {
							throw new ParserException(e.getMessage(), module, yyline + 1, yycolumn + 1);
						}

						return new Token(yyline, yycolumn, sym.RealLiteralExpCS, new Double(val)); 
					 }
        case 239: break;
        case 45: 
          {  str.append(yytext()); break;  }
        case 240: break;
        case 23: 
          {  return new Token(yyline, yycolumn, sym.PLUS);  }
        case 241: break;
        case 22: 
          {  return new Token(yyline, yycolumn, sym.VERT);  }
        case 242: break;
        case 7: 
          {  return new Token(yyline, yycolumn, sym.DOT);  }
        case 243: break;
        case 98: 
          {  return new Token(yyline, yycolumn, sym.FOR);  }
        case 244: break;
        case 103: 
          {  return new Token(yyline, yycolumn, sym.TRY);  }
        case 245: break;
        case 106: 
          {  return new Token(yyline, yycolumn, sym.NOP);  }
        case 246: break;
        case 108: 
          {  return new Token(yyline, yycolumn, sym.LET);  }
        case 247: break;
        case 130: 
          {  return new Token(yyline, yycolumn, sym.FROM);  }
        case 248: break;
        case 131: 
          {  return new Token(yyline, yycolumn, sym.THEN);  }
        case 249: break;
        case 135: 
          {  return new Token(yyline, yycolumn, sym.ELSE);  }
        case 250: break;
        case 136: 
          {  return new Token(yyline, yycolumn, sym.LINK);  }
        case 251: break;
        case 140: 
          {  return new Token(yyline, yycolumn, sym.WITH);  }
        case 252: break;
        case 143: 
          {  return new Token(yyline, yycolumn, sym.CASE);  }
        case 253: break;
        case 152: 
          {  return new Token(yyline, yycolumn, sym.BODY);  }
        case 254: break;
        case 46: 
          {  yybegin(YYINITIAL); return new Token(yyline, yycolumn, sym.StringLiteralExpCS, str.toString());  }
        case 255: break;
        case 61: 
          {  return new Token(yyline, yycolumn, sym.ASSIGN_DIVIDE);  }
        case 256: break;
        case 66: 
          {  return new Token(yyline, yycolumn, sym.GREATEREQUALS);  }
        case 257: break;
        case 49: 
          {  break;  }
        case 258: break;
        case 8: 
        case 11: 
          {  break;  }
        case 259: break;
        case 6: 
        case 50: 
        case 54: 
        case 95: 
        case 126: 
          { 
						int val;
						try {
							val = Integer.parseInt(yytext());
						}
						catch (Exception e) {
							throw new ParserException(e.getMessage(), module, yyline + 1, yycolumn + 1);
						}
						
						return new Token(yyline, yycolumn, sym.IntegerLiteralExpCS, new Integer(val)); 
					 }
        case 260: break;
        case 10: 
        case 27: 
        case 28: 
        case 29: 
        case 30: 
        case 31: 
        case 32: 
        case 33: 
        case 34: 
        case 35: 
        case 36: 
        case 37: 
        case 38: 
        case 39: 
        case 40: 
        case 41: 
        case 42: 
        case 43: 
        case 69: 
        case 70: 
        case 71: 
        case 72: 
        case 74: 
        case 75: 
        case 76: 
        case 77: 
        case 78: 
        case 79: 
        case 80: 
        case 81: 
        case 83: 
        case 84: 
        case 85: 
        case 86: 
        case 87: 
        case 88: 
        case 89: 
        case 90: 
        case 91: 
        case 92: 
        case 93: 
        case 94: 
        case 96: 
        case 97: 
        case 99: 
        case 100: 
        case 101: 
        case 102: 
        case 104: 
        case 105: 
        case 107: 
        case 109: 
        case 110: 
        case 111: 
        case 112: 
        case 113: 
        case 114: 
        case 115: 
        case 116: 
        case 117: 
        case 118: 
        case 119: 
        case 120: 
        case 122: 
        case 123: 
        case 124: 
        case 125: 
        case 127: 
        case 128: 
        case 129: 
        case 132: 
        case 134: 
        case 137: 
        case 138: 
        case 139: 
        case 141: 
        case 142: 
        case 144: 
        case 145: 
        case 146: 
        case 147: 
        case 148: 
        case 149: 
        case 150: 
        case 151: 
        case 154: 
        case 156: 
        case 159: 
        case 160: 
        case 161: 
        case 164: 
        case 165: 
        case 166: 
        case 167: 
        case 168: 
        case 169: 
        case 170: 
        case 175: 
        case 177: 
        case 179: 
        case 183: 
        case 184: 
        case 185: 
        case 186: 
        case 192: 
        case 193: 
        case 194: 
        case 196: 
        case 197: 
        case 201: 
          { 
						return new Token(yyline, yycolumn, sym.simpleNameCS, yytext());
					 }
        case 261: break;
        case 47: 
        case 48: 
          {  yybegin(YYINITIAL);  }
        case 262: break;
        case 60: 
          {  yybegin(BLOCKCOMMENT); break;  }
        case 263: break;
        case 59: 
          {  yybegin(LINECOMMENT); break;  }
        case 264: break;
        case 133: 
          {  return new Token(yyline, yycolumn, sym.BooleanLiteralExpCS, new Boolean(true));  }
        case 265: break;
        case 44: 
          {  str = new StringBuffer(); yybegin(STRING); break;  }
        case 266: break;
        case 121: 
          {  return new Token(yyline, yycolumn, sym.CollectionTypeIdentifierCS, yytext());  }
        case 267: break;
        case 58: 
          {  return new Token(yyline, yycolumn, sym.ASSIGN);  }
        case 268: break;
        case 25: 
          {  return new Token(yyline, yycolumn, sym.LOWER);  }
        case 269: break;
        case 24: 
          {  return new Token(yyline, yycolumn, sym.EQUAL);  }
        case 270: break;
        case 12: 
          {  return new Token(yyline, yycolumn, sym.DIVIDE);  }
        case 271: break;
        case 9: 
          {  return new Token(yyline, yycolumn, sym.COLON);  }
        case 272: break;
        case 5: 
          {  return new Token(yyline, yycolumn, sym.MINUS);  }
        case 273: break;
        case 13: 
          {  return new Token(yyline, yycolumn, sym.TIMES);  }
        case 274: break;
        case 21: 
          {  return new Token(yyline, yycolumn, sym.COMMA);  }
        case 275: break;
        case 157: 
          {  return new Token(yyline, yycolumn, sym.THROW);  }
        case 276: break;
        case 158: 
          {  return new Token(yyline, yycolumn, sym.ENDIF);  }
        case 277: break;
        case 162: 
          {  return new Token(yyline, yycolumn, sym.WHILE);  }
        case 278: break;
        case 163: 
          {  return new Token(yyline, yycolumn, sym.CATCH);  }
        case 279: break;
        case 171: 
          {  return new Token(yyline, yycolumn, sym.TUPLE);  }
        case 280: break;
        case 174: 
          {  return new Token(yyline, yycolumn, sym.INSERT);  }
        case 281: break;
        case 176: 
          {  return new Token(yyline, yycolumn, sym.SWITCH);  }
        case 282: break;
        case 178: 
          {  return new Token(yyline, yycolumn, sym.DELETE);  }
        case 283: break;
        case 180: 
          {  return new Token(yyline, yycolumn, sym.CREATE);  }
        case 284: break;
        case 181: 
          {  return new Token(yyline, yycolumn, sym.UNLINK);  }
        case 285: break;
        case 182: 
          {  return new Token(yyline, yycolumn, sym.RETURN);  }
        case 286: break;
        case 191: 
          {  return new Token(yyline, yycolumn, sym.REPLACE);  }
        case 287: break;
        case 190: 
          {  return new Token(yyline, yycolumn, sym.CONTEXT);  }
        case 288: break;
        case 189: 
          {  return new Token(yyline, yycolumn, sym.DEFAULT);  }
        case 289: break;
        case 26: 
          {  return new Token(yyline, yycolumn, sym.GREATER);  }
        case 290: break;
        case 14: 
          {  return new Token(yyline, yycolumn, sym.LEFTPAR);  }
        case 291: break;
        case 15: 
          {  return new Token(yyline, yycolumn, sym.RIGHTPAR);  }
        case 292: break;
        case 188: 
          {  return new Token(yyline, yycolumn, sym.FOREACH);  }
        case 293: break;
        case 203: 
        case 210: 
        case 213: 
        case 215: 
        case 216: 
        case 217: 
          {  	
						String dateval = yytext();
						
						Date val;
						try {
							val = DateUtils.parseDatetime(dateval);
						}
						catch (Exception e) {
							throw new ParserException(e.getMessage(), module, yyline + 1, yycolumn + 1);
						}

						return new Token(yyline, yycolumn, sym.DateLiteralExpCS, val); 
					 }
        case 294: break;
        default: 
          if (yy_input == YYEOF && yy_startRead == yy_currentPos) {
            yy_atEOF = true;
            yy_do_eof();
              { 	return new Token(yyline, yycolumn, sym.EOF);
 }
          } 
          else {
            yy_ScanError(YY_NO_MATCH);
          }
      }
    }
  }


}

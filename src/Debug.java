/**
 * Generic Debug class for printing to the console
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */
public class Debug {
    public static boolean DEBUG_MODE = true; // set to true to enable debugging
    public static void print(String message) {
        if (DEBUG_MODE) System.out.print(message);
    }
    public static void println(String message) {
        if (DEBUG_MODE) System.out.println(message);
    }
}

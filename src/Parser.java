/**
 * Hack Assembly Language Parser class - unpacks each instruction into its underlying fields
 * Encapsulates access to the input code. Reads an assembly language command, parses it, and
 * provides convenient access to the command's components (fields and symbols). In addition,
 * removes all white space and comments.
 * Considerations:
 * - Start reading a file with a given name
 * - Move to the next command in the file
 * -- Are we finished? boolean hasMoreCommands()
 * -- Get the next command void advance()
 * -- Need to read one line at a time
 * -- Need to skip whitespace including comments
 * Get the fields of the current command
 * -- Type of command: A (Address), C (Computation), L (Label pseudo-command)
 */

import java.nio.file.*;
import java.io.*;

public class Parser {
    private BufferedReader reader = null;
    private String line = null;
    public String currentCommand = null;
    public static final int A_COMMAND = 0;
    public static final int C_COMMAND = 1;
    public static final int L_COMMAND = 2;
    public Parser(String filename) {
        // Ensure the input file exists and is writable
        Path file = Paths.get(filename);
        InputStream input = null;
        try
        {
            file.getFileSystem().provider().checkAccess(file, AccessMode.READ);
            System.out.println("File exists and is readable");

            input = Files.newInputStream(file);
            this.reader = new BufferedReader(new InputStreamReader(input));
        }
        catch (IOException e)
        {
            System.out.println("I/O Exception: " + e);
        }
    }

    // Are we finished?
    boolean hasMoreCommands() {
        try
        {
            while ((line = reader.readLine()) != null)
            {
                // trim leading and trailing whitespace
                line = line.replaceAll("^\\s+|\\s+$", "");
                // remove comments
                line = line.replaceAll("//.*", "");
                if (line.isEmpty())
                {
                    continue;
                }
                return true;
            }
        }
        catch (IOException e)
        {
            System.out.println("I/O Exception: " + e);
        }
        return false;
    }

    // Get the next command
    void advance() {
        currentCommand = line;
    }

    // Get the type of command
    int commandType() {
        if (currentCommand.startsWith("@"))
        {
            return A_COMMAND;
        }
        else if (currentCommand.startsWith("("))
        {
            return L_COMMAND;
        }
        else
        {
            return C_COMMAND;
        }
    }

    // Get the symbol or decimal of the current command
    String symbol() {
        if (commandType() == A_COMMAND)
        {
            return currentCommand.substring(1);
        }
        else if (commandType() == L_COMMAND)
        {
            return currentCommand.substring(1, currentCommand.length() - 1);
        }
        else if (commandType() != A_COMMAND && commandType() != L_COMMAND)
        {
            throw new IllegalArgumentException("Command is not an A or L command");
        }
        return null;
    }

    // Get the dest mnemonic in the current C-command
    String dest() {
        if (commandType() == C_COMMAND)
        {
            if (currentCommand.contains("="))
            {
                return currentCommand.split("=")[0];
            }
        }
        else
        {
            // throw an exception
            if (commandType() != C_COMMAND)
            {
                throw new IllegalArgumentException("Command is not a C-command");
            }
        }
        return null;
    }
}

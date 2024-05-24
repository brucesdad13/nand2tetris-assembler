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
 * -- Need to skip whitespace including comment
 * Get the fields of the current command
 * -- Type of command: A (Address), C (Computation), L (Label pseudo-command)
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */

import java.nio.file.*;
import java.io.*;

public class Parser {
    private BufferedReader reader = null;
    private String line = null;

    public String currentCommand = null;
    public static final int A_COMMAND = 0; // address command
    public static final int C_COMMAND = 1; // computation command
    public static final int L_COMMAND = 2; // label pseudo-command

    public Parser(String filename) {
        // Ensure the input file exists and is writable
        Path file = Paths.get(filename);
        InputStream input = null;
        // open the input file for reading
        try
        {
            file.getFileSystem().provider().checkAccess(file, AccessMode.READ); // check access
            input = Files.newInputStream(file);
            this.reader = new BufferedReader(new InputStreamReader(input));
        }
        catch (IOException e)
        {
            System.out.println("Hack Assembler Input File I/O Exception: " + e);
        }
    }

    // Are we finished? Check the file for additional commands
    boolean hasMoreCommands() {
        try
        {
            while ((line = reader.readLine()) != null) // while not end of file
            {
                // remove comments
                line = line.replaceAll("//.*", "");

                // remove whitespace
                line = line.replaceAll("\\s+", "");

                if (line.isEmpty()) continue; // ignore empty lines

                return true;
            }
        }
        catch (IOException e)
        {
            System.out.println("Hack Assembler Input File I/O Exception: " + e);
        }
        return false;
    }

    // Get the next command per API
    void advance() {
        currentCommand = line;
    }

    // Get the type of command per API
    int commandType() {
        if (currentCommand.startsWith("@")) // A command
        {
            return A_COMMAND;
        }
        else if (currentCommand.startsWith("(")) // label pseudo-command
        {
            return L_COMMAND;
        }
        else // assume C command
        {
            return C_COMMAND;
        }
    }

    // Get the symbol or decimal of the current command
    String symbol() {
        if (commandType() == A_COMMAND)
        {
            return currentCommand.substring(1); // remove the @
        }
        else if (commandType() == L_COMMAND)
        {
            return currentCommand.substring(1, currentCommand.length() - 1); // remove the parentheses
        }
        else if (commandType() != A_COMMAND && commandType() != L_COMMAND)
        {
            throw new IllegalArgumentException("Command is not an A or L command"); // not valid for C commands
        }
        return null;
    }

    // Get the dest mnemonic in the current C-command
    String dest() {
        if (commandType() == C_COMMAND)
        {
            if (currentCommand.contains("="))
            {
                return currentCommand.split("=")[0]; // the destination is before the equals sign
            }
        }
        else
        {
            // throw an exception
            if (commandType() != C_COMMAND)
            {
                throw new IllegalArgumentException("Command is not a C-command"); // not valid for A or L commands
            }
        }
        return null;
    }

    // Get the comp mnemonic in the current C-command
    // API Note: Either the dest or jump fields may be empty
    String comp() {
        String command = "";
        if (commandType() == C_COMMAND)
        {
            if (currentCommand.contains("=")) // if the command has a destination field
            {
                command = currentCommand.split("=")[1]; // isolate the computation from the destination field
            }
            if (currentCommand.contains(";")) // if the command has a jump field
            {
                command = currentCommand.split(";")[0]; // isolate the computation from the jump field
            }
            return command; // return the computation expression
        }
        else
        {
            // throw an exception; comp is only valid for C commands
            if (commandType() != C_COMMAND)
            {
                throw new IllegalArgumentException("Command is not a C-command"); // not valid for A or L commands
            }
        }
        return null;
    }

    // Get the jump mnemonic in the current C-command
    String jump() {
        if (commandType() == C_COMMAND)
        {
            if (currentCommand.contains(";"))
            {
                return currentCommand.split(";")[1];
            }
        }
        else
        {
            // throw an exception
            if (commandType() != C_COMMAND)
            {
                throw new IllegalArgumentException("Command is not a C-command"); // not valid for A or L commands
            }
        }
        return null;
    }

    // Close the Hack Assembler input file
    public void close() {
        try
        {
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("I/O Exception: " + e);
        }
    }
}

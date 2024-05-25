/**
 * NAND to Tetris CPU 16-bit assembler. Converts human-readable instructions to 16-bit machine code.
 * Initializes the I/O files and drives the process.
 * <p>
 * Hack Machine Language reference:
 * The most significant bit is the opcode specifying the instruction type:
 * 0: A-instruction
 * 1: C-instruction
 * Note: If the opcode is 1 then the next 2 bits are ignored and set to 1 by convention.
 * <p>
 * The next 6 bits are the ALU control bits. Which can be represented as a truth table
 * The next 3 bits are the destination register control bits. Which can be represented as a truth table
 * The final 3 bits are the jump control bits. Which can be represented as a truth table
 * <p>
 *  by Charles Stevenson (brucesdad13@gmail.com)
 *  Revision History:
 *  2024-05-24: Initial version
 */

import java.nio.file.*;
import java.io.*;

public class Main {
    /**
     * Main entry point for the Hack Assembler
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Ensure the input file and output file are provided as command line arguments
        if (args.length != 2)
        {
            System.out.println("Usage: java Main <input file> <output file>");
            return;
        }

        // Ensure the output file exists and is writable
        Path outputPath = Paths.get(args[1]);
        try
        {
            outputPath.getFileSystem().provider().checkAccess(outputPath, AccessMode.WRITE);
            Debug.println("File exists and is writable");
        }
        catch (IOException e)
        {
            Debug.println("I/O Exception: " + e);
        }

        SymbolTable symbolTable = new SymbolTable(); // instantiate the SymbolTable class

        // First pass: in the first pass we are only concerned with labels e.g. (LOOP)
        Parser parser = new Parser(args[0]); // args[0] is the input file
        parseInput(parser, symbolTable, outputPath, true); // true means this is the first pass

        // Second pass: generate the machine code and write it to the output file
        // new parser object to force rewind the input file to the beginning
        // symbol table is already populated with labels from the first pass
        parser = new Parser(args[0]); // args[0] is the input file
        parseInput(parser, symbolTable, outputPath, false); // false means this is not the first pass

        // Close the Hack Assembler input file and exit
        parser.close();
    }


    /**
     * Parse the input file, build the symbol table, and generate the Hack machine code
     * Note: On the first pass, populate the symbol table with label line numbers
     *       On the second pass, generate machine code
     * @param parser the Parser object
     * @param symbolTable the SymbolTable object
     * @param outputPath the Path object for the output file
     * @param firstPass boolean flag to indicate if this is the first pass
     */
    public static void parseInput(Parser parser, SymbolTable symbolTable, Path outputPath, boolean firstPass) {
        Code code = new Code(); // instantiate the Code class
        BufferedWriter writer = null; // output file writer
        int lineNumber = 0; // line number counter initialized to 0
        int variableAddress = 16; // variable address counter initialized to 16 (after general purpose registers)
        String binary = ""; // text representation of the binary machine code

        if (!firstPass) // on the second pass, open the output file for writing
        {
            try {
                writer = new BufferedWriter(new FileWriter(outputPath.toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        while (parser.hasMoreCommands())
        {
            parser.advance(); // advance to the next command in the file starting from line 0

            Debug.print(lineNumber + ": ");
            Debug.println(parser.currentCommand);

            if (parser.commandType() == Parser.A_COMMAND && !firstPass) // ignore A-commands on the first pass
            {
                Debug.println(" // A-instruction; address = " + parser.symbol());

                // if the symbol is a numeric constant, convert it to binary
                if (parser.symbol().matches("\\d+"))
                {
                    binary = Integer.toBinaryString(Integer.parseInt(parser.symbol())); // convert the number to binary
                }
                else // if the symbol is a variable or label
                {
                    // if the symbol is missing from the symbol table, add it as a variable
                    // labels are already in the symbol table from the first pass L_COMMAND stage
                    if (!symbolTable.contains(parser.symbol()))
                    {
                        // if the symbol is a variable, add it to the symbol table starting at address 16 (after registers)
                        symbolTable.addEntry(parser.symbol(), variableAddress);
                        variableAddress++; // increment the variable address counter
                    }
                    // convert the symbol to binary
                    binary = Integer.toBinaryString(symbolTable.getAddress(parser.symbol()));
                }
                // Construct final Hack ML instruction; MSB 0 means A-instruction plus 15-bit address 0nnnnnnnnnnnnnnn
                binary = "0" + "0".repeat(15 - binary.length()) + binary; // Pad with leading zeros to 15 bits

                Debug.println(" // Machine code: " + binary);
            }
            else if (parser.commandType() == Parser.C_COMMAND && !firstPass) // ignore C-commands on the first pass
            {
                Debug.println(" // C-instruction; " + parser.dest() + "=" + parser.comp() + ";" + parser.jump());

                // MSB 1 means C-instruction, next two bits are 1 by convention and unused/ignored
                binary = "111" + code.comp(parser.comp()) + code.dest(parser.dest()) + code.jump(parser.jump());

                Debug.println(" // Machine code: " + binary);
            }
            else if (parser.commandType() == Parser.L_COMMAND && firstPass) // only process L-commands on the first pass
            {
                Debug.println(" // Label; " + parser.symbol());

                // Check if the label is already in the symbol table
                // Note: Duplicate label declarations are not allowed and are assumed to be erroneous
                if (symbolTable.contains(parser.symbol()))
                    throw new IllegalArgumentException("Label already exists in the symbol table");
                else
                    symbolTable.addEntry(parser.symbol(), lineNumber);
            }

            // Append the binary machine code to the output file
            try {
                if (writer != null && parser.commandType() != Parser.L_COMMAND) // ignore labels
                {
                    // Ensure the binary machine code is 16 bits as a sanity check
                    if (binary.length() != 16)
                        throw new IllegalArgumentException("Invalid machine code length: " + binary.length());

                    Debug.println("Writing to file: " + binary);

                    writer.write(binary); // Write the binary Hack machine code as text according to the API spec.
                    writer.newLine(); // Write a newline character to separate the machine code instructions
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (parser.commandType() != Parser.L_COMMAND) // ignore labels from the line count
                lineNumber++; // Increment the line number (whitespace, labels, and comments are not counted)
        }

        if (Debug.DEBUG_MODE) // print symbol table for debugging
        {
            Debug.println("Symbol table:");
            symbolTable.printTable(); // print the symbol table
        }

        // Close the Hack Machine Language output file
        try {
            if (writer != null) writer.close(); // close the output file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

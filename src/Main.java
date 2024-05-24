/**
 * NAND to Tetris CPU 16-bit assembler. Converts human-readable instructions to 16-bit machine code.
 * Initializes the I/O files and drives the process.
 * Hack Machine Language reference:
 * The most significant bit is the opcode specifying the instruction type:
 * 0: A-instruction
 * 1: C-instruction
 * Note: If the opcode is 1 then the next 2 bits are ignored.
 *
 * The next 6 bits are the ALU control bits. Which can be represented as a truth table
 * The next 3 bits are the destination register control bits. Which can be represented as a truth table
 * The final 3 bits are the jump control bits. Which can be represented as a truth table
 *
 * c-bits => instruct ALU which function to compute
 * Truth table: instruction[6..11]
 * Computation  c1 c2 c3 c4 c5 c6
 * +-----+-----+-----------------
 * |  0  |     | 1  0  1  0  1  0
 * |  1  |     | 1  1  1  1  1  1
 * | -1  |     | 1  1  1  0  1  0
 * |  D  |     | 0  0  1  1  0  0
 * |  A  |  M  | 1  1  0  0  0  0
 * | !D  |     | 0  0  1  1  0  1
 * | !A  | !M  | 1  1  0  0  0  1
 * | -D  |     | 0  0  1  1  1  1
 * | -A  | -M  | 1  1  0  0  1  1
 * | D+1 |     | 0  1  1  1  1  1
 * | A+1 | M+1 | 1  1  0  1  1  1
 * | D-1 |     | 0  0  1  1  1  0
 * | A-1 | M-1 | 1  1  0  0  1  0
 * | D+A | D+M | 0  0  0  0  1  0
 * | D-A | D-M | 0  1  0  0  1  1
 * | A-D | M-D | 0  0  0  1  1  1
 * | D&A | D&M | 0  0  0  0  0  0
 * | D|A | D|M | 0  1  0  1  0  1
 * +-----+-----+-----------------
 * | a=0 | a=1 <= a-bit value 0 or 1
 *
 * d-bits => instruct ALU where to store output
 * Truth table: instruction[3..5]
 * mnem.  d1 d2 d3 desc.
 * null | 0  0  0 | The value is not stored
 *  M   | 0  0  1 | RAM[A]
 *  D   | 0  1  0 | D register
 * MD   | 0  1  1 | RAM[A] and D register
 *  A   | 1  0  0 | A register
 * AM   | 1  0  1 | A register and RAM[A]
 * AD   | 1  1  0 | A register and D register
 * AMD  | 1  1  1 | A register, RAM[A], and D register
 *        A  D  M
 * j-bits => specify optional jump condition
 * Truth table: instruction[0..2]
 * mnem.  j1 j2 j3 logic
 * null | 0  0  0 | no jump // PC++ next instruction will be current PC plus 1
 *  JGT | 0  0  1 | if out > 0 jump // And(Not(zr),Not(ng)) not zero AND not negative
 *  JEQ | 0  1  0 | if out == 0 jump // And(zr,Not(ng)) is equal to zero AND not negative
 *  JGE | 0  1  1 | if out >= 0 jump // Or(zr,Not(ng)) is equal to zero OR is not negative
 *  JLT | 1  0  0 | if out < 0 jump // And(Not(zr),ng) is not zero AND is negative
 *  JNE | 1  0  1 | if out != 0 jump // Not(zr)
 *  JLE | 1  1  0 | if out <= 0 jump // Or(zr,ng) is equal to zero OR is negative
 *  JMP | 1  1  1 | unconditional jump // true ... PC=A next instruction will be the address stored in A register (already loaded)
 *
 *  by Charles Stevenson (brucesdad13@gmail.com)
 *  Revision History:
 *  2024-05-24: Initial version
 */

import java.nio.file.*;
import java.io.*;

public class Main {
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

        // First pass: build the symbol table
        Parser parser = new Parser(args[0]); // args[0] is the input file
        parseInput(parser, symbolTable, outputPath, true); // true means this is the first pass

        // Second pass: generate the machine code and write it to the output file
        // new parser object to force rewind the input file to the beginning
        parser = new Parser(args[0]); // args[0] is the input file
        parseInput(parser, symbolTable, outputPath, false); // false means this is not the first pass

        // Close the Hack Assembler input file and exit
        parser.close();
    }

    // create a function that parses the input file, takes a boolean parameter of whether this is the first pass or not
    // on the first pass, build the symbol table, otherwise generate machine code
    public static void parseInput(Parser parser, SymbolTable symbolTable, Path outputPath, boolean firstPass) {
        Code code = new Code(); // instantiate the Code class
        BufferedWriter writer = null; // output file writer
        int lineNumber = 0; // line number counter initialized to 0
        int variableAddress = 16; // variable address counter initialized to 16
        String binary = ""; // store the binary machine code

        if (firstPass)
        {
            Debug.println("First pass: build the symbol table"); // FIXME: debugging
        }
        else
        {
            Debug.println("Second pass: generate the machine code"); // FIXME: debugging

            // open the output file for writing
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

            if (parser.commandType() == Parser.A_COMMAND && !firstPass) // ignore on the first pass
            {
                Debug.println(" // A-instruction; address = " + parser.symbol()); // FIXME: debugging

                // if the symbol is a number, convert it to binary
                if (parser.symbol().matches("\\d+"))
                {
                    binary = Integer.toBinaryString(Integer.parseInt(parser.symbol())); // convert the number to binary
                }
                else
                {
                    // if the symbol is a label, look it up in the symbol table
                    // and convert it to binary
                    if (symbolTable.contains(parser.symbol()))
                    {
                        binary = Integer.toBinaryString(symbolTable.getAddress(parser.symbol()));
                    }
                    else
                    {
                        Debug.println(" // New Variable: " + parser.symbol()); // FIXME: debugging
                        // if the symbol is a variable, add it to the symbol table
                        symbolTable.addEntry(parser.symbol(), variableAddress);
                        binary = Integer.toBinaryString(variableAddress);
                        variableAddress++; // increment the variable address counter
                    }
                    binary = Integer.toBinaryString(symbolTable.getAddress(parser.symbol()));
                }
                // Hack ML instruction MSB 0 means A-instruction plus 15-bit address
                binary = "0" + "0".repeat(15 - binary.length()) + binary; // pad with leading zeros to 15 bits

                Debug.println(" // Machine code: " + binary); // FIXME: debugging
            }
            else if (parser.commandType() == Parser.C_COMMAND && !firstPass) // ignore on the first pass
            {
                Debug.println(" // C-instruction; " + parser.dest() + "=" + parser.comp() + ";" + parser.jump()); // FIXME: debugging

                // MSB 1 means C-instruction, next two bits are 1 by convention and unused/ignored
                binary = "111" + code.comp(parser.comp()) + code.dest(parser.dest()) + code.jump(parser.jump());

                Debug.println(" // Machine code: " + binary); // FIXME: debugging
            }
            else if (parser.commandType() == Parser.L_COMMAND && firstPass) // only process on the first pass
            {
                Debug.println(" // Label; " + parser.symbol()); // FIXME: debugging

                // check if the label is already in the symbol table
                if (symbolTable.contains(parser.symbol()))
                    throw new IllegalArgumentException("Label already exists in the symbol table");
                else
                    symbolTable.addEntry(parser.symbol(), lineNumber);
                continue; // don't increment the line number for labels
            }

            // Append the binary machine code to the output file
            try {
                if (writer != null)
                {
                    Debug.println("Writing to file: " + binary); // FIXME: debugging

                    writer.write(binary);
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            lineNumber++; // Increment the line number (whitespace and comments are not counted)
        }

        // print symbol table for debugging // FIXME: hide output if not debugging
        if (Debug.DEBUG_MODE && firstPass)
        {
            Debug.println("After first pass, symbol table:");
            symbolTable.printTable();
        }

        // Close the Hack Machine Language output file
        try {
            if (writer != null)
            {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

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
            System.out.println("File exists and is writable");
        }
        catch (IOException e)
        {
            System.out.println("I/O Exception: " + e);
        }

        // First pass: build the symbol table
        Parser parser = new Parser(args[0]); // args[0] is the input file
        parseInput(parser, outputPath, true);

        // Second pass: generate the machine code and write it to the output file
        // new parser object to force rewind the input file to the beginning
        parser = new Parser(args[0]);
        parseInput(parser, outputPath, false);

        // Close the file I/O
        parser.close();
    }

    // create a function that parses the input file, takes a boolean parameter of whether this is the first pass or not
    // on the first pass, build the symbol table, otherwise generate machine code
    public static void parseInput(Parser parser, Path outputPath, boolean firstPass) {
        int lineNumber = 0;
        Code code = new Code();
        String binary = ""; // store the binary machine code
        BufferedWriter writer = null;
        SymbolTable symbolTable = new SymbolTable();

        if (firstPass)
        {
            System.out.println("First pass: build the symbol table");
        }
        else
        {
            System.out.println("Second pass: generate the machine code");
            // open the output file for writing
            try {
                writer = new BufferedWriter(new FileWriter(outputPath.toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        while (parser.hasMoreCommands())
        {
            parser.advance();
            System.out.print(lineNumber + ": ");
            System.out.print(parser.currentCommand);
            // get the type of command
            //System.out.println("; //Command type " + parser.commandType());
            if (parser.commandType() == Parser.A_COMMAND)
            {
                System.out.println(" // A-instruction; address = " + parser.symbol());
                // if this is the first pass and the command is an A-instruction
                // add the symbol to the symbol table
                if (firstPass)
                {
                    // throw unimplemented exception
                    //throw new UnsupportedOperationException("Not implemented yet");
                    // if the symbol is a number, ignore it
                    if (parser.symbol().matches("\\d+"))
                    {
                        continue;
                    }
                    // if the symbol is a label, add it to the symbol table
                    symbolTable.addEntry(parser.symbol(), lineNumber);
                }
                else
                {
                    // if this is the second pass, generate the machine code
                    // if the symbol is a number, convert it to binary
                    if (parser.symbol().matches("\\d+"))
                    {
                        binary = Integer.toBinaryString(Integer.parseInt(parser.symbol()));
                    }
                    else
                    {
                        // if the symbol is a label, look it up in the symbol table
                        // and convert it to binary
                        binary = Integer.toBinaryString(symbolTable.getAddress(parser.symbol()));
                    }
                    // pad the binary number with leading zeros to 15 bits
                    binary = "0".repeat(16 - binary.length()) + binary;
                    System.out.println(" // Machine code: " + binary);
                }
            }
            else if (parser.commandType() == Parser.C_COMMAND)
            {
                System.out.println(" // C-instruction; " + parser.dest() + "=" + parser.comp() + ";" + parser.jump());
                binary = "111" + code.comp(parser.comp()) + code.dest(parser.dest()) + code.jump(parser.jump());
                System.out.println(" // Machine code: " + binary);
            }
            else if (parser.commandType() == Parser.L_COMMAND)
            {
                System.out.println(" // Label; " + parser.symbol());
                // if this is the first pass and the command is a label
                // add the label to the symbol table with the current line number
                if (firstPass)
                    // check if the label is already in the symbol table
                    if (symbolTable.contains(parser.symbol()))
                        throw new IllegalArgumentException("Label already exists in the symbol table");
                    else
                        symbolTable.addEntry(parser.symbol(), lineNumber);
                // else ignore the label on subsequent passes
            }
            try {
                if (writer != null)
                {
                    System.out.println("Writing to file: " + binary);
                    writer.write(binary);
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            lineNumber++;
        }

        // print symbol table for debugging // FIXME: hide output if not debugging
        if (firstPass)
        {
            System.out.println("Symbol table:");
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

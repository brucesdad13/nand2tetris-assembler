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
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        /*
        //int instruction = 0b1110110000010000; // C instruction
        int instruction = 0b1111010011110000;
        //int instruction = 0b1110000111110000; // C instruction
        //int instruction = 0b0101101110100000; // A instruction
        // print whether this is an A instruction or a C instruction in text
        System.out.println("Instruction type: " + (instruction >> 15 == 0 ? "A" : "C"));
        // if this is an A instruction print the 15-bit address in binary and exit
        if (instruction >> 15 == 0) {
            System.out.println("Address: " + Integer.toBinaryString(instruction & 0b0111111111111111));
            return;
        } else {
            // print the 3-bit destination register control bit representation as text
            int destControl = (instruction >> 3) & 0b111;
            switch (destControl) {
                case 0b000 -> System.out.print("null");
                case 0b001 -> System.out.print("M");
                case 0b010 -> System.out.print("D");
                case 0b011 -> System.out.print("MD");
                case 0b100 -> System.out.print("A");
                case 0b101 -> System.out.print("AM");
                case 0b110 -> System.out.print("AD");
                case 0b111 -> System.out.print("AMD");
                default -> System.out.println("Unknown");
            }
            System.out.print("=");
            // if this is a C instruction print the 6-bit ALU control bit representation as text
            // a-bit (instruction[12]) determines if the ALU control bits are for A or M
            int aBit = (instruction >> 12) & 0b1;
            int aluControl = (instruction >> 6) & 0b111111;
            switch (aluControl) {
                case 0b101010 -> System.out.print("0");
                case 0b111111 -> System.out.print("1");
                case 0b111010 -> System.out.print("-1");
                case 0b001100 -> System.out.print("D");
                case 0b110000 -> System.out.print((aBit == 0 ? "A" : "M"));
                case 0b001101 -> System.out.print("!D");
                case 0b110001 -> System.out.print((aBit == 0 ? "!A" : "!M"));
                case 0b001111 -> System.out.print("-D");
                case 0b110011 -> System.out.print((aBit == 0 ? "-A" : "-M"));
                case 0b011111 -> System.out.print("D+1");
                case 0b110111 -> System.out.print((aBit == 0 ? "A+1" : "M+1"));
                case 0b001110 -> System.out.print("D-1");
                case 0b110010 -> System.out.print((aBit == 0 ? "A-1" : "M-1"));
                case 0b000010 -> System.out.print("D+A");
                case 0b010011 -> System.out.print("D-A");
                case 0b000111 -> System.out.print("A-D");
                case 0b000000 -> System.out.print("D&A");
                case 0b010101 -> System.out.print("D|A");
                default -> System.out.println("Unknown");
            }
            System.out.println();
            // print the 3-bit jump control bit representation as text
            int jumpControl = instruction & 0b111;
            switch (jumpControl) {
                case 0b000 -> System.out.println("Jump Control: " + "null");
                case 0b001 -> System.out.println("Jump Control: " + "JGT");
                case 0b010 -> System.out.println("Jump Control: " + "JEQ");
                case 0b011 -> System.out.println("Jump Control: " + "JGE");
                case 0b100 -> System.out.println("Jump Control: " + "JLT");
                case 0b101 -> System.out.println("Jump Control: " + "JNE");
                case 0b110 -> System.out.println("Jump Control: " + "JLE");
                case 0b111 -> System.out.println("Jump Control: " + "JMP");
                default -> System.out.println("Jump Control: " + "Unknown");
            }
        }
         */

        if (args.length != 2)
        {
            System.out.println("Usage: java Main <input file> <output file>");
            return;
        }

        // Parse the input file
        Parser parser = new Parser(args[0]); // args[0] is the input file
        int lineNumber = 0;
        while (parser.hasMoreCommands())
        {
            parser.advance();
            System.out.print(lineNumber + ": ");
            System.out.print(parser.currentCommand);
            // get the type of command
            System.out.println("; //Command type " + parser.commandType());
            lineNumber++;
        }

        // First pass: build the symbol table
        // skip for now

        // Second pass: generate the machine code
        // proof of concept parse a set of hardcoded assembly language instructions
        // and output the machine code to the standard output
        // new parser object

        // Write the machine code to the output file

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


        // Close the file I/O

    }
}

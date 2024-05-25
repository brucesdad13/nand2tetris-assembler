/**
 * Hack Assembly Language Code class - translates Hack Assembly Language mnemonics into binary code.
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */
public class Code {
    /**
     * Convert destination into binary per the API truth table.
     * Note: M register is the RAM[A] register.
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
     * AMD  | 1  1  1 | A register, D register, and RAM[A]
     *        A  D  M
     * Comment: The mnemonic AMD could perhaps more accurately have been ADM
     * preserving the order of the registers in the truth table bit positions.
     * @param mnemonic the destination mnemonic
     * @return String the destination binary code
     */
    public String dest(String mnemonic) {
        return switch (mnemonic) {
            case "M" -> // memory register
                    "001";
            case "D" -> // data register
                    "010";
            case "MD" -> // memory and data register
                    "011";
            case "A" -> // address register
                    "100";
            case "AM" -> // address and memory register
                    "101";
            case "AD" -> // address and data register
                    "110";
            case "AMD" -> // address, data, and memory register
                    "111";
            case null, default -> "000";
        };
    }

    /**
     * Convert computation into binary per the Hack Assembly language API truth table.
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
     * TODO: make switch more elegant and less redundant by support a-bit
     * @param mnemonic the computation mnemonic
     * @return String the computation binary code
     */
    public String comp(String mnemonic) {
        return switch (mnemonic) {
            case "0" -> // zero
                    "0101010";
            case "1" -> // one
                    "0111111";
            case "-1" -> // negative one
                    "0111010";
            case "D" -> // D register
                    "0001100";
            case "A" -> // A register
                    "0110000";
            case "!D" -> // not D (complement)
                    "0001101";
            case "!A" -> // not A (complement)
                    "0110001";
            case "-D" -> // negate D
                    "0001111";
            case "-A" -> // negate A
                    "0110011";
            case "D+1" -> // D plus 1 (increment D)
                    "0011111";
            case "A+1" -> // A plus 1 (increment A)
                    "0110111";
            case "D-1" -> // D minus 1 (decrement D)
                    "0001110";
            case "A-1" -> // A minus 1 (decrement A)
                    "0110010";
            case "D+A" -> // D plus A
                    "0000010";
            case "D-A" -> // D minus A
                    "0010011";
            case "A-D" -> // A minus D
                    "0000111";
            case "D&A" -> // D AND A (bitwise AND)
                    "0000000";
            case "D|A" -> // D OR A (bitwise OR)
                    "0010101";
            case "M" -> // M register
                    "1110000";
            case "!M" -> // not M (complement)
                    "1110001";
            case "-M" -> // negate M
                    "1110011";
            case "M+1" -> // M plus 1 (increment M)
                    "1110111";
            case "M-1" -> // M minus 1 (decrement M)
                    "1110010";
            case "D+M" -> // D plus M
                    "1000010";
            case "D-M" -> // D minus M
                    "1010011";
            case "M-D" -> // M minus D
                    "1000111";
            case "D&M" -> // D AND M (bitwise AND)
                    "1000000";
            case "D|M" -> // D OR M (bitwise OR)
                    "1010101"; // no computation
            case null, default -> "0000000";
        };
    }

    /**
     * Convert jump into binary per the Hack Assembly language API truth table.
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
     * @param mnemonic the jump mnemonic
     * @return String the jump binary code
     */
    public String jump(String mnemonic) {
        return switch (mnemonic) {
            case "JGT" -> // if out > 0 jump
                    "001";
            case "JEQ" -> // if out == 0 jump
                    "010";
            case "JGE" -> // if out >= 0 jump
                    "011";
            case "JLT" -> // if out < 0 jump
                    "100";
            case "JNE" -> // if out != 0 jump
                    "101";
            case "JLE" -> // if out <= 0 jump
                    "110";
            case "JMP" -> // unconditional jump
                    "111"; // no jump
            case null, default -> "000";
        };
    }
}

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
        switch (mnemonic) {
            case "M": // memory register
                return "001";
            case "D": // data register
                return "010";
            case "MD": // memory and data register
                return "011";
            case "A": // address register
                return "100";
            case "AM": // address and memory register
                return "101";
            case "AD": // address and data register
                return "110";
            case "AMD": // address, data, and memory register
                return "111";
            case null:
            default:
                return "000";
        }
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
        switch (mnemonic) {
            case "0": // zero
                return "0101010";
            case "1": // one
                return "0111111";
            case "-1": // negative one
                return "0111010";
            case "D": // D register
                return "0001100";
            case "A": // A register
                return "0110000";
            case "!D": // not D (complement)
                return "0001101";
            case "!A": // not A (complement)
                return "0110001";
            case "-D": // negate D
                return "0001111";
            case "-A": // negate A
                return "0110011";
            case "D+1": // D plus 1 (increment D)
                return "0011111";
            case "A+1": // A plus 1 (increment A)
                return "0110111";
            case "D-1": // D minus 1 (decrement D)
                return "0001110";
            case "A-1": // A minus 1 (decrement A)
                return "0110010";
            case "D+A": // D plus A
                return "0000010";
            case "D-A": // D minus A
                return "0010011";
            case "A-D": // A minus D
                return "0000111";
            case "D&A": // D AND A (bitwise AND)
                return "0000000";
            case "D|A": // D OR A (bitwise OR)
                return "0010101";
            case "M": // M register
                return "1110000";
            case "!M": // not M (complement)
                return "1110001";
            case "-M": // negate M
                return "1110011";
            case "M+1": // M plus 1 (increment M)
                return "1110111";
            case "M-1": // M minus 1 (decrement M)
                return "1110010";
            case "D+M": // D plus M
                return "1000010";
            case "D-M": // D minus M
                return "1010011";
            case "M-D": // M minus D
                return "1000111";
            case "D&M": // D AND M (bitwise AND)
                return "1000000";
            case "D|M": // D OR M (bitwise OR)
                return "1010101";
            case null: // no computation
            default:
                return "0000000";
        }
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
        switch (mnemonic) {
            case "JGT": // if out > 0 jump
                return "001";
            case "JEQ": // if out == 0 jump
                return "010";
            case "JGE": // if out >= 0 jump
                return "011";
            case "JLT": // if out < 0 jump
                return "100";
            case "JNE": // if out != 0 jump
                return "101";
            case "JLE": // if out <= 0 jump
                return "110";
            case "JMP": // unconditional jump
                return "111";
            case null: // no jump
            default:
                return "000";
        }
    }
}

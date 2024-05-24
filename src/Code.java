/**
 * Hack Assembly Language Code class - translates Hack Assembly Language mnemonics into binary code.
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */
public class Code {
    // convert destination into binary per the API truth table
    // API Note: M register is the RAM[A] register
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
            case "AMD": // address, memory, and data register
                return "111";
            case null:
            default:
                return "000";
        }
    }

    // convert computation into binary per the Hack Assembly language API table
    // TODO: make switch more elegant and less redundant by support a-bit
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

    // Convert jump into binary per the Hack Assembly language API truth table.
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

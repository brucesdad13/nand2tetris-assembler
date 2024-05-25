/**
 * Hack Assembly Language SymbolTable class - manages the symbol table
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */
import java.util.*;
public class SymbolTable {
    /**
     * The symbol table is a HashMap that contains the predefined symbols
     * and their corresponding memory locations. The predefined symbols
     * are the registers, the memory-mapped screen and keyboard, and the
     * general purpose registers R0-R15. Additional predefined symbols
     * include the stack pointer, local, argument, this, and that.
     * The table is initialized with the predefined symbols and then new
     * symbols are added as they are encountered in the assembly code.
     * Reference the Hack Assembler API
     */
    private final Map<String, Integer> table = new HashMap<>() {{
        put("SP", 0); // stack pointer
        put("LCL", 1); // local
        put("ARG", 2); // argument
        put("THIS", 3); // this
        put("THAT", 4); // that
        put("R0", 0); // R0-R15 are general purpose registers
        put("R1", 1);
        put("R2", 2);
        put("R3", 3);
        put("R4", 4);
        put("R5", 5);
        put("R6", 6);
        put("R7", 7);
        put("R8", 8);
        put("R9", 9);
        put("R10", 10);
        put("R11", 11);
        put("R12", 12);
        put("R13", 13);
        put("R14", 14);
        put("R15", 15);
        put("SCREEN", 16384); // memory-mapped screen
        put("KBD", 24576); // memory-mapped keyboard
    }};

    /**
     * Add a new symbol to the table
     * @param symbol the symbol to add
     * @param address the address of the symbol
     */
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    /**
     * Does the symbol table contain the given symbol?
     * @param symbol the symbol to check
     * @return boolean true if the symbol is in the table
     */
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    /**
     * Get the address of the given symbol
     * @param symbol the symbol to get the address of
     * @return int the address of the symbol
     */
    public int getAddress(String symbol) {
        return table.get(symbol);
    }

    /**
     * Print the symbol table
     */
    public void printTable() {
        // print symbol table sorted by value numerically
        table.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(System.out::println);
    }
}

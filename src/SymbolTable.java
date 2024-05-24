/**
 * Hack Assembly Language SymbolTable class - manages the symbol table
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */
import java.util.*;
public class SymbolTable {
    // Add the predefined symbols to the table
    private Map<String, Integer> table = new HashMap<String, Integer>() {{
        put("SP", 0);
        put("LCL", 1);
        put("ARG", 2);
        put("THIS", 3);
        put("THAT", 4);
        put("R0", 0);
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
        put("SCREEN", 16384);
        put("KBD", 24576);
    }};
    // Add a new symbol to the table
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }
    // Does the symbol table contain the given symbol?
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }
    // Get the address of the given symbol
    public int getAddress(String symbol) {
        return table.get(symbol);
    }

    public void printTable() {
        // print symbol table sorted by value numerically
        table.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .forEach(System.out::println);
    }
}

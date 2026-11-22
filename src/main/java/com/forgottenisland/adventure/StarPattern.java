package com.forgottenisland.adventure;

import java.util.ArrayList;
import java.util.List;

/** Fixed north-facing, five-point floor plan. Coordinates are relative to the activator. */
public final class StarPattern {
    public record Cell(int x, int z) {}
    public static final List<String> PLAN = List.of(
        "    #    ",
        "    #    ",
        "   #.#   ",
        "###...###",
        "#.......#",
        " #..C..# ",
        "  #...#  ",
        "  #.#.#  ",
        " ### ### ",
        " #     # ");
    public static final List<Cell> FRAME = cells('#');
    public static final List<Cell> OPENING = cells('.', 'C');
    private static List<Cell> cells(char... chars) {
        var result = new ArrayList<Cell>();
        for (int z=0;z<PLAN.size();z++) for (int x=0;x<PLAN.get(z).length();x++) {
            for(char c:chars) if(PLAN.get(z).charAt(x)==c) result.add(new Cell(x-4,z-5));
        }
        return List.copyOf(result);
    }
    private StarPattern() {}
}

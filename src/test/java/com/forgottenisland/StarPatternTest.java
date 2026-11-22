package com.forgottenisland;

import com.forgottenisland.adventure.StarPattern;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class StarPatternTest {
    @Test void recipeSuppliesTheDocumentedFivePointFrame() {
        assertEquals(27,StarPattern.FRAME.size());
        assertEquals(21,StarPattern.OPENING.size());
        assertTrue(StarPattern.OPENING.contains(new StarPattern.Cell(0,0)));
        assertTrue(StarPattern.FRAME.size()+1<=32);
        var all=new HashSet<>(StarPattern.FRAME);
        for(var c:StarPattern.OPENING)assertTrue(all.add(c),"Border overlaps opening");
        for(var c:all)assertTrue(all.contains(new StarPattern.Cell(-c.x(),c.z())),"Star is not symmetric");
        assertTrue(all.contains(new StarPattern.Cell(0,-5)));
        assertTrue(all.contains(new StarPattern.Cell(-4,-2)));
        assertTrue(all.contains(new StarPattern.Cell(4,-2)));
        assertTrue(all.contains(new StarPattern.Cell(-3,4)));
        assertTrue(all.contains(new StarPattern.Cell(3,4)));
    }
    @Test void openingIsConnectedForJumpEntry() {
        var visited=new HashSet<StarPattern.Cell>();
        visited.add(new StarPattern.Cell(0,0));
        boolean added;
        do {added=false;for(var c:StarPattern.OPENING)if(!visited.contains(c)&&visited.stream().anyMatch(v->Math.abs(v.x()-c.x())+Math.abs(v.z()-c.z())==1))added|=visited.add(c);} while(added);
        assertEquals(new HashSet<>(StarPattern.OPENING),visited);
    }
}

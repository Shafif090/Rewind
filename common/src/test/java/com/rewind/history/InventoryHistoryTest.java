package com.rewind.history;

import com.rewind.action.InventoryAction;
import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class InventoryHistoryTest {
    @Test
    void pushKeepsLatestActionOnTop() {
        InventoryHistory history = new InventoryHistory(2);
        InventoryAction first = action("First");
        InventoryAction second = action("Second");

        history.push(first);
        history.push(second);

        assertSame(second, history.peek().orElseThrow());
        assertEquals(2, history.size());
    }

    @Test
    void pushDiscardsOldestActionWhenFull() {
        InventoryHistory history = new InventoryHistory(2);
        InventoryAction first = action("First");
        InventoryAction second = action("Second");
        InventoryAction third = action("Third");

        history.push(first);
        history.push(second);
        history.push(third);

        assertSame(third, history.pop().orElseThrow());
        assertSame(second, history.pop().orElseThrow());
        assertEquals(0, history.size());
    }

    @Test
    void constructorRejectsInvalidMaxSize() {
        assertThrows(IllegalArgumentException.class, () -> new InventoryHistory(0));
    }

    private static InventoryAction action(String description) {
        return new InventoryAction() {
            @Override
            public boolean canUndo(UndoContext context) {
                return true;
            }

            @Override
            public UndoResult undo(UndoContext context) {
                return UndoResult.success(description);
            }

            @Override
            public String description() {
                return description;
            }
        };
    }
}

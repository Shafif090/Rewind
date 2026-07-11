package com.rewind.capture;

import com.rewind.action.RecordedInventoryAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts slot-level before/after changes into normalized inventory actions.
 */
public final class InventoryTransactionDetector {
    private static final String UNEXPECTED_CHANGE = "unexpected inventory change";

    private InventoryTransactionDetector() {
    }

    public static ActionDetectionResult detect(
        Collection<SlotState> before,
        Collection<SlotState> after,
        InventoryActionHint hint
    ) {
        Objects.requireNonNull(before, "before");
        Objects.requireNonNull(after, "after");
        Objects.requireNonNull(hint, "hint");

        List<SlotDelta> deltas = deltas(before, after);
        if (deltas.isEmpty()) {
            return ActionDetectionResult.ignored("no slot changes");
        }

        return switch (hint) {
            case DROP -> detectDrop(deltas);
            case PICKUP -> detectPickup(deltas);
            case SWAP -> detectSwap(deltas, InventoryActionKind.SWAP);
            case HOTBAR_SWAP -> detectSwap(deltas, InventoryActionKind.HOTBAR_SWAP);
            case MOVE -> detectMove(deltas);
            case SHIFT_CLICK -> detectShiftClick(deltas);
            case SORT -> detectSort(deltas);
            case UNKNOWN -> detectUnknown(deltas);
        };
    }

    private static ActionDetectionResult detectUnknown(List<SlotDelta> deltas) {
        List<ActionDetectionResult> attempts = List.of(
            detectSwap(deltas, InventoryActionKind.SWAP),
            detectMove(deltas),
            detectShiftClick(deltas),
            detectDrop(deltas),
            detectPickup(deltas),
            detectSort(deltas)
        );
        return attempts.stream()
            .filter(ActionDetectionResult::isDetected)
            .findFirst()
            .orElseGet(() -> ActionDetectionResult.ignored(UNEXPECTED_CHANGE));
    }

    private static ActionDetectionResult detectMove(List<SlotDelta> deltas) {
        if (deltas.size() != 2) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        SlotDelta loss = onlyLoss(deltas);
        SlotDelta gain = onlyGain(deltas);
        if (loss == null || gain == null || !loss.before().sameStackIdentity(gain.after())) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }
        if (loss.lostCount() != gain.gainedCount()) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        return detected(InventoryActionKind.MOVE, deltas, "Moved Stack");
    }

    private static ActionDetectionResult detectSwap(List<SlotDelta> deltas, InventoryActionKind kind) {
        if (deltas.size() != 2) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        SlotDelta first = deltas.getFirst();
        SlotDelta second = deltas.getLast();
        boolean swapped = first.before().sameStackAndCount(second.after())
            && second.before().sameStackAndCount(first.after());
        if (!swapped) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        return detected(kind, deltas, kind == InventoryActionKind.HOTBAR_SWAP ? "Hotbar Swap" : "Swapped Slots");
    }

    private static ActionDetectionResult detectShiftClick(List<SlotDelta> deltas) {
        List<SlotDelta> losses = losses(deltas);
        List<SlotDelta> gains = gains(deltas);
        if (losses.size() != 1 || gains.isEmpty()) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        SlotDelta loss = losses.getFirst();
        int gained = 0;
        for (SlotDelta gain : gains) {
            if (!loss.before().sameStackIdentity(gain.after())) {
                return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
            }
            gained += gain.gainedCount();
        }
        if (loss.lostCount() != gained) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        return detected(InventoryActionKind.SHIFT_CLICK, deltas, "Shift-click Transfer");
    }

    private static ActionDetectionResult detectDrop(List<SlotDelta> deltas) {
        if (deltas.stream().anyMatch(SlotDelta::isGain)) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }
        if (losses(deltas).isEmpty()) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }
        if (!sameLossIdentity(deltas)) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        return detected(InventoryActionKind.DROP, deltas, "Dropped Item");
    }

    private static ActionDetectionResult detectPickup(List<SlotDelta> deltas) {
        if (deltas.stream().anyMatch(SlotDelta::isLoss)) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }
        if (gains(deltas).isEmpty()) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }
        if (!sameGainIdentity(deltas)) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        return detected(InventoryActionKind.PICKUP, deltas, "Picked Up Item");
    }

    private static ActionDetectionResult detectSort(List<SlotDelta> deltas) {
        if (deltas.size() < 2 || !sameMultiset(deltas)) {
            return ActionDetectionResult.ignored(UNEXPECTED_CHANGE);
        }

        return detected(InventoryActionKind.SORT, deltas, "Sorted Inventory");
    }

    private static ActionDetectionResult detected(InventoryActionKind kind, List<SlotDelta> deltas, String description) {
        return ActionDetectionResult.detected(new RecordedInventoryAction(kind, deltas, description));
    }

    private static List<SlotDelta> deltas(Collection<SlotState> before, Collection<SlotState> after) {
        Map<Integer, SlotState> beforeBySlot = bySlot(before);
        Map<Integer, SlotState> afterBySlot = bySlot(after);
        ArrayList<Integer> slotIndexes = new ArrayList<>();
        slotIndexes.addAll(beforeBySlot.keySet());
        for (int slot : afterBySlot.keySet()) {
            if (!beforeBySlot.containsKey(slot)) {
                slotIndexes.add(slot);
            }
        }
        slotIndexes.sort(Comparator.naturalOrder());

        ArrayList<SlotDelta> result = new ArrayList<>();
        for (int slot : slotIndexes) {
            SlotState beforeState = beforeBySlot.getOrDefault(slot, SlotState.empty(slot));
            SlotState afterState = afterBySlot.getOrDefault(slot, SlotState.empty(slot));
            if (!beforeState.sameStackAndCount(afterState)) {
                result.add(new SlotDelta(slot, beforeState, afterState));
            }
        }
        return result;
    }

    private static Map<Integer, SlotState> bySlot(Collection<SlotState> states) {
        HashMap<Integer, SlotState> result = new HashMap<>();
        for (SlotState state : states) {
            SlotState previous = result.put(state.slotIndex(), state);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate slot state for slot " + state.slotIndex());
            }
        }
        return result;
    }

    private static SlotDelta onlyLoss(List<SlotDelta> deltas) {
        List<SlotDelta> losses = losses(deltas);
        return losses.size() == 1 ? losses.getFirst() : null;
    }

    private static SlotDelta onlyGain(List<SlotDelta> deltas) {
        List<SlotDelta> gains = gains(deltas);
        return gains.size() == 1 ? gains.getFirst() : null;
    }

    private static List<SlotDelta> losses(List<SlotDelta> deltas) {
        return deltas.stream().filter(SlotDelta::isLoss).toList();
    }

    private static List<SlotDelta> gains(List<SlotDelta> deltas) {
        return deltas.stream().filter(SlotDelta::isGain).toList();
    }

    private static boolean sameLossIdentity(List<SlotDelta> deltas) {
        StackFingerprint expected = null;
        for (SlotDelta delta : losses(deltas)) {
            if (expected == null) {
                expected = delta.before().stack();
            } else if (!expected.equals(delta.before().stack())) {
                return false;
            }
        }
        return expected != null;
    }

    private static boolean sameGainIdentity(List<SlotDelta> deltas) {
        StackFingerprint expected = null;
        for (SlotDelta delta : gains(deltas)) {
            if (expected == null) {
                expected = delta.after().stack();
            } else if (!expected.equals(delta.after().stack())) {
                return false;
            }
        }
        return expected != null;
    }

    private static boolean sameMultiset(List<SlotDelta> deltas) {
        Map<StackFingerprint, Integer> beforeCounts = new HashMap<>();
        Map<StackFingerprint, Integer> afterCounts = new HashMap<>();
        for (SlotDelta delta : deltas) {
            addCount(beforeCounts, delta.before());
            addCount(afterCounts, delta.after());
        }
        return beforeCounts.equals(afterCounts);
    }

    private static void addCount(Map<StackFingerprint, Integer> counts, SlotState state) {
        if (!state.isEmpty()) {
            counts.merge(state.stack(), state.count(), Integer::sum);
        }
    }
}

package com.rewind.action;

import com.rewind.capture.SlotDelta;
import com.rewind.capture.StackFingerprint;

import java.util.List;

final class ActionDeltaRules {
    private ActionDeltaRules() {
    }

    static void requireMove(List<SlotDelta> deltas) {
        if (deltas.size() != 2) {
            throw new IllegalArgumentException("move requires exactly two deltas");
        }
        SlotDelta loss = singleLoss(deltas, "move");
        SlotDelta gain = singleGain(deltas, "move");
        if (!loss.before().sameStackIdentity(gain.after()) || loss.lostCount() != gain.gainedCount()) {
            throw new IllegalArgumentException("move deltas must transfer the same stack and count");
        }
    }

    static void requireSwap(List<SlotDelta> deltas, String actionName) {
        if (deltas.size() != 2) {
            throw new IllegalArgumentException(actionName + " requires exactly two deltas");
        }
        SlotDelta first = deltas.getFirst();
        SlotDelta second = deltas.getLast();
        boolean swapped = first.before().sameStackAndCount(second.after())
            && second.before().sameStackAndCount(first.after());
        if (!swapped) {
            throw new IllegalArgumentException(actionName + " deltas must swap two slot states");
        }
    }

    static void requireShiftClick(List<SlotDelta> deltas) {
        SlotDelta loss = singleLoss(deltas, "shift-click");
        int gained = 0;
        int gainCount = 0;
        for (SlotDelta delta : deltas) {
            requireLossOrGain(delta, "shift-click");
            if (delta.isGain()) {
                gainCount++;
                if (!loss.before().sameStackIdentity(delta.after())) {
                    throw new IllegalArgumentException("shift-click gains must match the lost stack");
                }
                gained += delta.gainedCount();
            }
        }
        if (gainCount == 0 || loss.lostCount() != gained) {
            throw new IllegalArgumentException("shift-click deltas must conserve transferred count");
        }
    }

    static void requirePickup(List<SlotDelta> deltas) {
        requireOnlyGains(deltas, "pickup");
        requireSameGainIdentity(deltas, "pickup");
    }

    static void requireDrop(List<SlotDelta> deltas) {
        requireOnlyLosses(deltas, "drop");
        requireSameLossIdentity(deltas, "drop");
    }

    private static SlotDelta singleLoss(List<SlotDelta> deltas, String actionName) {
        SlotDelta loss = null;
        for (SlotDelta delta : deltas) {
            if (delta.isLoss()) {
                if (loss != null) {
                    throw new IllegalArgumentException(actionName + " requires exactly one loss");
                }
                loss = delta;
            }
        }
        if (loss == null) {
            throw new IllegalArgumentException(actionName + " requires exactly one loss");
        }
        return loss;
    }

    private static SlotDelta singleGain(List<SlotDelta> deltas, String actionName) {
        SlotDelta gain = null;
        for (SlotDelta delta : deltas) {
            if (delta.isGain()) {
                if (gain != null) {
                    throw new IllegalArgumentException(actionName + " requires exactly one gain");
                }
                gain = delta;
            }
        }
        if (gain == null) {
            throw new IllegalArgumentException(actionName + " requires exactly one gain");
        }
        return gain;
    }

    private static void requireOnlyGains(List<SlotDelta> deltas, String actionName) {
        boolean sawGain = false;
        for (SlotDelta delta : deltas) {
            requireLossOrGain(delta, actionName);
            if (delta.isLoss()) {
                throw new IllegalArgumentException(actionName + " deltas must not include losses");
            }
            sawGain |= delta.isGain();
        }
        if (!sawGain) {
            throw new IllegalArgumentException(actionName + " requires at least one gain");
        }
    }

    private static void requireOnlyLosses(List<SlotDelta> deltas, String actionName) {
        boolean sawLoss = false;
        for (SlotDelta delta : deltas) {
            requireLossOrGain(delta, actionName);
            if (delta.isGain()) {
                throw new IllegalArgumentException(actionName + " deltas must not include gains");
            }
            sawLoss |= delta.isLoss();
        }
        if (!sawLoss) {
            throw new IllegalArgumentException(actionName + " requires at least one loss");
        }
    }

    private static void requireSameGainIdentity(List<SlotDelta> deltas, String actionName) {
        StackFingerprint expected = null;
        for (SlotDelta delta : deltas) {
            if (delta.isGain()) {
                expected = requireSameIdentity(expected, delta.after().stack(), actionName);
            }
        }
    }

    private static void requireSameLossIdentity(List<SlotDelta> deltas, String actionName) {
        StackFingerprint expected = null;
        for (SlotDelta delta : deltas) {
            if (delta.isLoss()) {
                expected = requireSameIdentity(expected, delta.before().stack(), actionName);
            }
        }
    }

    private static StackFingerprint requireSameIdentity(
        StackFingerprint expected,
        StackFingerprint actual,
        String actionName
    ) {
        if (expected == null) {
            return actual;
        }
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(actionName + " deltas must use one stack identity");
        }
        return expected;
    }

    private static void requireLossOrGain(SlotDelta delta, String actionName) {
        if (!delta.isLoss() && !delta.isGain()) {
            throw new IllegalArgumentException(actionName + " deltas must only include losses or gains");
        }
    }
}

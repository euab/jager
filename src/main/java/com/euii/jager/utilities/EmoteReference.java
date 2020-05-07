package com.euii.jager.utilities;

import java.util.Optional;

public enum EmoteReference {
    WAVING_HAND(":wave", "\u1ffb"),
    WARNING("warning", "\u26a0"),
    REPEAT(":repeat_button:", "\u1f501"),
    PAUSE(":pause_button:", "\u23f8"),
    PLAY(":arrow_forward:", "\u25b6"),
    COUNTERCLOCKWISE_ARROWS(":arrows_counterclockwise:", "\u1f504"),
    CRYING_FACE(":cry:", "\u1f622"),
    SLIGHT_FROWN(":slight_frown:", "\u1f641"),
    HOURGLASS(":hourglass:", "\u231b"),
    STOPWATCH(":stopwatch:", "\u23f1");

    final String notation;
    final String unicode;

    EmoteReference(String notation, String unicode) {
        this.notation = notation;
        this.unicode = unicode;
    }

    @Override
    public String toString() {
        return Optional.ofNullable(unicode).orElse(notation) + " ";
    }

    public String getNotation() {
        return notation;
    }

    public String getUnicode() {
        return unicode;
    }
}

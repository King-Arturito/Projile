package io.github.kingarturito.projile.shared;

import io.github.kingarturito.projile.shared.exceptions.InvalidPositionException;

import java.util.regex.Pattern;

/**
 * Lexicographic fractional index ("LexoRank"-style) used to order siblings
 * without rewriting their neighbours: board columns and tasks within a column.
 */
public record Position(String value) {

    /** Midpoint of the first bucket: leaves room to insert both before and after. */
    public static final Position INITIAL = new Position("0|hzzzzz:");

    private static final Pattern VALID_FORMAT = Pattern.compile("[0-9A-Za-z|:]{1,64}");

    public Position {
        if (value == null || !VALID_FORMAT.matcher(value).matches()) {
            throw new InvalidPositionException(
                    "Position must be a 1-64 character fractional index (base-62 with '|' and ':').");
        }
    }
}

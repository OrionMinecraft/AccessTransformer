package eu.mikroskeem.at;

import eu.mikroskeem.at.access.AccessLevel;
import eu.mikroskeem.at.access.Modifiers;
import eu.mikroskeem.shuriken.common.Ensure;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parsed access transform entry
 *
 * @author Mark Vainomaa
 */
@Getter
@ToString
public class AccessTransformEntry {
    private final AccessLevel accessLevel;
    private final List<Modifiers.ModifierEntry> modifiers;
    private final String className;
    private final String descriptor;
    private final boolean method;

    public AccessTransformEntry(String rawAtEntry) {
        String[] atEntry = rawAtEntry.split(" ");
        String[] modifiers = atEntry[0].split("(?=[-+])");
        this.accessLevel = AccessLevel.BY_NAME.get(modifiers[0]);
        this.modifiers = Stream.of(Arrays.copyOfRange(modifiers, 1, modifiers.length)).map(entry -> {
            Modifiers modifier = Modifiers.BY_NAME.get(entry.substring(1));
            Ensure.notNull(modifier, "Invalid modifier");
            char action = entry.charAt(0);
            if(!(action == '-' || action == '+')) {
                throw new RuntimeException("Invalid modifier action: " + action);
            }
            return new Modifiers.ModifierEntry(action == '-', modifier);
        }).collect(Collectors.toList());
        this.className = atEntry[1];
        this.descriptor = atEntry[2];

        /* Check if transform entry is for method or field */
        int start = descriptor.indexOf('(');
        if(start != -1) {
            int end = descriptor.indexOf(')', start);
            if(end != -1) {
                method = true;
            } else {
                throw new RuntimeException("Invalid AT entry");
            }
        } else {
            method = false;
        }
    }
}

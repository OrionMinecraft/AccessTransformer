package eu.mikroskeem.at.access;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents field/method modifiers
 *
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
@Getter
public enum Modifiers {
    FINAL("f", Opcodes.ACC_FINAL);

    /** Access level name */
    private final String name;

    /** Access level opcode */
    private final int opcode;

    /** Static list of opcodes mapped to enum keys */
    public final static Map<Integer, Modifiers> BY_OPCODE;

    /** Static list of opcode names mapped to enum keys */
    public final static Map<String, Modifiers> BY_NAME;

    static {
        BY_OPCODE = Collections.unmodifiableMap(new HashMap<Integer, Modifiers>(){{
            for (Modifiers value : Modifiers.values()) {
                put(value.opcode, value);
            }
        }});

        BY_NAME = Collections.unmodifiableMap(new HashMap<String, Modifiers>(){{
            for (Modifiers value : Modifiers.values()) {
                put(value.name, value);
            }
        }});
    }

    @RequiredArgsConstructor
    @Getter
    public static class ModifierEntry {
        /** If given modifier must be removed or added */
        private final boolean remove;

        /** Target modifier */
        private final Modifiers modifier;
    }
}

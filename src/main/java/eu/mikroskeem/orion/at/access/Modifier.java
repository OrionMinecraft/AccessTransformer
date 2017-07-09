package eu.mikroskeem.orion.at.access;

import org.jetbrains.annotations.Contract;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents field/method modifiers
 *
 * @author Mark Vainomaa
 */
public enum Modifier {
    FINAL("f", Opcodes.ACC_FINAL);

    /** Access level name */
    private final String name;

    /** Access level opcode */
    private final int opcode;

    /**
     * {@link Modifier} enum constructor
     *
     * @param name Modifier name
     * @param opcode Modifier opcode, see {@link Opcodes}
     */
    Modifier(String name, int opcode) {
        this.name = name;
        this.opcode = opcode;
    }

    /**
     * Gets modifier name
     *
     * @return Modifier name
     */
    @Contract(pure = true)
    public String getName() {
        return name;
    }

    /**
     * Gets modifier opcode
     *
     * @return Modifier opcode
     */
    @Contract(pure = true)
    public int getOpcode() {
        return opcode;
    }

    /** Static list of opcodes mapped to enum keys */
    public final static Map<Integer, Modifier> BY_OPCODE;

    /** Static list of opcode names mapped to enum keys */
    public final static Map<String, Modifier> BY_NAME;

    static {
        BY_OPCODE = Collections.unmodifiableMap(new HashMap<Integer, Modifier>(){{
            for (Modifier value : Modifier.values()) {
                put(value.opcode, value);
            }
        }});

        BY_NAME = Collections.unmodifiableMap(new HashMap<String, Modifier>(){{
            for (Modifier value : Modifier.values()) {
                put(value.name, value);
            }
        }});
    }

    /**
     * AT Modifier entry
     */
    public static class ModifierEntry {
        /** If given modifier must be removed or added */
        private final boolean remove;

        /** Target modifier */
        private final Modifier modifier;

        /**
         * ModifierEntry constructor
         *
         * @param remove Whether modifier must be added or removed
         * @param modifier {@link Modifier}
         */
        public ModifierEntry(boolean remove, Modifier modifier) {
            this.remove = remove;
            this.modifier = modifier;
        }

        /**
         * Returns whether given modifier should be added or removed
         *
         * @return
         */
        @Contract(pure = true)
        public boolean isRemove() {
            return remove;
        }

        /**
         * Returns {@link Modifier} what should be applied to given element (class, method, field)
         *
         * @return {@link Modifier} instance
         */
        @Contract(pure = true)
        public Modifier getModifier() {
            return modifier;
        }
    }
}

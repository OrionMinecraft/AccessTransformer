package eu.mikroskeem.orion.at.access;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents field/method access modifiers
 *
 * @author Mark Vainomaa
 */
public enum AccessModifier {
    FINAL("f", Opcodes.ACC_FINAL);

    /** Access level name */
    private final String name;

    /** Access level opcode */
    private final int opcode;

    /**
     * {@link AccessModifier} enum constructor
     *
     * @param name AccessModifier name
     * @param opcode AccessModifier opcode, see {@link Opcodes}
     */
    AccessModifier(String name, int opcode) {
        this.name = name;
        this.opcode = opcode;
    }

    /**
     * Gets accessModifier name
     *
     * @return AccessModifier name
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Gets accessModifier opcode
     *
     * @return AccessModifier opcode
     */
    public int getOpcode() {
        return opcode;
    }

    /** Static list of opcodes mapped to enum keys */
    public final static Map<Integer, AccessModifier> BY_OPCODE;

    /** Static list of opcode names mapped to enum keys */
    public final static Map<String, AccessModifier> BY_NAME;

    static {
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<Integer, AccessModifier> byOpcode;
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<String, AccessModifier> byName;
        BY_OPCODE = Collections.unmodifiableMap((byOpcode = new HashMap<>(AccessModifier.values().length)));
        BY_NAME = Collections.unmodifiableMap((byName  = new HashMap<>(AccessModifier.values().length)));

        for (AccessModifier value : AccessModifier.values()) {
            byOpcode.put(value.opcode, value);
            byName.put(value.name, value);
        }
    }

    /**
     * AT AccessModifier entry
     */
    public static class ModifierEntry {
        /** If given modifier must be removed or added */
        private final boolean remove;

        /** Target modifier */
        private final AccessModifier accessModifier;

        /**
         * ModifierEntry constructor
         *
         * @param remove Whether given access modifier must be added or removed
         * @param accessModifier {@link AccessModifier}
         */
        public ModifierEntry(boolean remove, @NonNull AccessModifier accessModifier) {
            this.remove = remove;
            this.accessModifier = Objects.requireNonNull(accessModifier, "accessModifier must be not null");
        }

        /**
         * Returns whether given access modifier should be added or removed
         *
         * @return Whether given access modifier should be added or removed
         */
        public boolean isRemove() {
            return remove;
        }

        /**
         * Returns {@link AccessModifier} what should be applied to given element (class, method, field)
         *
         * @return {@link AccessModifier} instance
         */
        @NonNull
        public AccessModifier getAccessModifier() {
            return accessModifier;
        }
    }
}

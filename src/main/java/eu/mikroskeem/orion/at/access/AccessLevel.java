package eu.mikroskeem.orion.at.access;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents method/field access level
 *
 * @author Mark Vainomaa
 */
public enum AccessLevel {
    PRIVATE("private", Opcodes.ACC_PRIVATE),
    PACKAGE_LOCAL("default", 0),
    PROTECTED("protected", Opcodes.ACC_PROTECTED),
    PUBLIC("public", Opcodes.ACC_PUBLIC);

    /** Access level name */
    private final String name;

    /** Access level opcode */
    private final int opcode;

    /**
     * {@link AccessLevel} enum constructor
     *
     * @param name Access level name
     * @param opcode Access level opcode, see {@link Opcodes}
     */
    AccessLevel(String name, int opcode) {
        this.name = name;
        this.opcode = opcode;
    }

    /**
     * Gets access level name
     *
     * @return Access level name
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * Gets opcode value
     *
     * @return Opcode value
     */
    public int getOpcode() {
        return opcode;
    }

    /** Static list of opcodes mapped to enum keys */
    public final static Map<Integer, AccessLevel> BY_OPCODE;

    /** Static list of opcode names mapped to enum keys */
    public final static Map<String, AccessLevel> BY_NAME;

    /**
     * Overrides class/method/field access level by {@link AccessLevel}
     *
     * @param accessLevel Original class/method/field access level
     * @param newAccessLevel {@link AccessLevel} to apply on given class/method/field access level
     * @return New class/method/field access level
     */
    public static int overrideAccessLevel(int accessLevel, AccessLevel newAccessLevel) {
        int mask = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;
        accessLevel &= ~mask;
        accessLevel |= newAccessLevel.opcode;
        return accessLevel;
    }

    static {
        Map<Integer, AccessLevel> byOpcode;
        Map<String, AccessLevel> byName;
        BY_OPCODE = Collections.unmodifiableMap((byOpcode = new HashMap<>(AccessLevel.values().length)));
        BY_NAME = Collections.unmodifiableMap((byName  = new HashMap<>(AccessLevel.values().length)));

        for (AccessLevel value : AccessLevel.values()) {
            byOpcode.put(value.opcode, value);
        }

        for (AccessLevel value : AccessLevel.values()) {
            byName.put(value.name, value);
        }
    }
}

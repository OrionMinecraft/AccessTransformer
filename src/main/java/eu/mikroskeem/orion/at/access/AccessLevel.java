package eu.mikroskeem.orion.at.access;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    AccessLevel(@NonNull String name, int opcode) {
        this.name = Objects.requireNonNull(name, "name must not be null");
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
        /* Do not allow downgrades */
        if(java.lang.reflect.Modifier.isPublic(accessLevel) && newAccessLevel.ordinal() <= AccessLevel.PUBLIC.ordinal())
            return accessLevel;
        if(java.lang.reflect.Modifier.isProtected(accessLevel) && newAccessLevel.ordinal() <= AccessLevel.PROTECTED.ordinal())
            return accessLevel;
        if(!java.lang.reflect.Modifier.isPrivate(accessLevel) && newAccessLevel.ordinal() <= AccessLevel.PACKAGE_LOCAL.ordinal())
            return accessLevel;
        if(java.lang.reflect.Modifier.isPrivate(accessLevel) && newAccessLevel.ordinal() <= AccessLevel.PRIVATE.ordinal())
            return accessLevel;

        /* Replace all access levels with desired one */
        int mask = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;
        accessLevel &= ~mask;
        accessLevel |= newAccessLevel.opcode;
        return accessLevel;
    }

    static {
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<Integer, AccessLevel> byOpcode;
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<String, AccessLevel> byName;
        BY_OPCODE = Collections.unmodifiableMap((byOpcode = new HashMap<>(AccessLevel.values().length)));
        BY_NAME = Collections.unmodifiableMap((byName  = new HashMap<>(AccessLevel.values().length)));

        for (AccessLevel value : AccessLevel.values()) {
            byOpcode.put(value.opcode, value);
            byName.put(value.name, value);
        }
    }
}

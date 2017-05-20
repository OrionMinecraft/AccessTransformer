package eu.mikroskeem.at.access;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents method/field access level
 *
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
@Getter
public enum AccessLevel {
    PRIVATE("private", Opcodes.ACC_PRIVATE),
    PACKAGE_LOCAL("default", 0),
    PROTECTED("protected", Opcodes.ACC_PROTECTED),
    PUBLIC("public", Opcodes.ACC_PUBLIC);

    /** Access level name */
    private final String name;

    /** Access level opcode */
    private final int opcode;

    /** Static list of opcodes mapped to enum keys */
    public final static Map<Integer, AccessLevel> BY_OPCODE;

    /** Static list of opcode names mapped to enum keys */
    public final static Map<String, AccessLevel> BY_NAME;

    /** Override access level */
    @Contract(pure = true)
    public static int overrideAccessLevel(int accessLevel, AccessLevel newAccessLevel) {
        int mask = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;
        accessLevel &= ~mask;
        accessLevel |= newAccessLevel.opcode;
        return accessLevel;
    }

    static {
        BY_OPCODE = Collections.unmodifiableMap(new HashMap<Integer, AccessLevel>(){{
            for (AccessLevel value : AccessLevel.values()) {
                put(value.opcode, value);
            }
        }});

        BY_NAME = Collections.unmodifiableMap(new HashMap<String, AccessLevel>(){{
            for (AccessLevel value : AccessLevel.values()) {
                put(value.name, value);
            }
        }});
    }
}

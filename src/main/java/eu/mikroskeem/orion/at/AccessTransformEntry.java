package eu.mikroskeem.orion.at;

import eu.mikroskeem.orion.at.access.AccessLevel;
import eu.mikroskeem.orion.at.access.Modifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parsed access transform entry
 *
 * @author Mark Vainomaa
 */
public final class AccessTransformEntry {
    @NotNull private final AccessLevel accessLevel;
    @NotNull private final List<Modifier.ModifierEntry> modifiers;
    @NotNull private final String className;
    private final String descriptor;
    private final boolean method;
    private final boolean classAt;

    /**
     * Parses raw access transformer entry from string
     *
     * @param rawAtEntry Raw AT entry line
     */
    public AccessTransformEntry(@NotNull String rawAtEntry) {
        String[] atEntry = rawAtEntry.split(" ");
        String[] modifiers = atEntry[0].split("(?=[-+])");
        classAt = atEntry.length == 2;
        this.accessLevel = AccessLevel.BY_NAME.get(modifiers[0]);
        this.modifiers = Stream.of(Arrays.copyOfRange(modifiers, 1, modifiers.length)).map(entry -> {
            char action = entry.charAt(0);
            Modifier modifier = Modifier.BY_NAME.get(entry.substring(1));

            /* Validate */
            if(modifier == null) throw new IllegalStateException("Invalid modifier: " + entry.substring(1));
            if(action != '-' && action != '+') throw new IllegalStateException("Invalid modifier action: " + action);
            return new Modifier.ModifierEntry(action == '-', modifier);
        }).collect(Collectors.toList());
        this.className = atEntry[1];
        this.descriptor = classAt ? null : atEntry[2];

        /* If it is class AT, then return */
        if(classAt) {
            method = false;
            return;
        }

        /* Check if transform entry is for method or field */
        int start = descriptor.indexOf('(');
        if(start != -1) {
            int end = descriptor.indexOf(')', start);
            if(end != -1) {
                method = true;
            } else {
                throw new IllegalStateException("Invalid method AT entry: " + rawAtEntry);
            }
        } else {
            method = false;
        }
    }

    /**
     * Gets {@link AccessTransformEntry}'s {@link AccessLevel}
     *
     * @return Instance of {@link AccessLevel}
     */
    @NotNull
    @Contract(pure = true)
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Gets {@link AccessTransformEntry}'s {@link Modifier.ModifierEntry} list
     *
     * @return List of {@link Modifier.ModifierEntry}
     */
    @NotNull
    @Contract(pure = true)
    public List<Modifier.ModifierEntry> getModifiers() {
        return modifiers;
    }

    /**
     * Gets target class name (method/field owner or class itself)
     *
     * @return Target class name
     */
    @NotNull
    @Contract(pure = true)
    public String getClassName() {
        return className;
    }

    /**
     * Gets target field/method descriptor (field does not have <pre>()V</pre>-like descriptor though)
     *
     * @return Target method descriptor
     */
    @Contract(pure = true)
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Returns whether {@link AccessTransformEntry} targets method or not
     *
     * @return Whether {@link AccessTransformEntry} targets method or not
     */
    @Contract(pure = true)
    public boolean isMethod() {
        return method;
    }

    /**
     * Returns whether {@link AccessTransformEntry} targets class or not
     *
     * @return Whether {@link AccessTransformEntry} targets class or not
     */
    @Contract(pure = true)
    public boolean isClassAt() {
        return classAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AccessTransformEntry that = (AccessTransformEntry) o;

        if (method != that.method)
            return false;
        if (classAt != that.classAt)
            return false;
        if (accessLevel != that.accessLevel)
            return false;
        if (!modifiers.equals(that.modifiers))
            return false;
        if (!className.equals(that.className))
            return false;
        return descriptor != null ? descriptor.equals(that.descriptor) : that.descriptor == null;
    }

    @Override
    public int hashCode() {
        int result = accessLevel.hashCode();
        result = 31 * result + modifiers.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + (descriptor != null ? descriptor.hashCode() : 0);
        result = 31 * result + (method ? 1 : 0);
        result = 31 * result + (classAt ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AccessTransformEntry{accessLevel=" + accessLevel + ", modifiers=" + modifiers + ", className='"
                + className + '\'' + ", descriptor='" + descriptor + '\'' + ", method=" + method + ", classAt=" +
                classAt + '}';
    }
}

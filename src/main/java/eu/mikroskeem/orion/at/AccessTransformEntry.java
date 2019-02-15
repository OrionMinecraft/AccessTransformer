package eu.mikroskeem.orion.at;

import eu.mikroskeem.orion.at.access.AccessLevel;
import eu.mikroskeem.orion.at.access.Modifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parsed access transform entry
 *
 * @author Mark Vainomaa
 */
public final class AccessTransformEntry {
    @NonNull private final AccessLevel accessLevel;
    @NonNull private final List<Modifier.ModifierEntry> modifiers;
    @NonNull private final String className;
    private final String descriptor;
    private final boolean method;
    private final boolean classAt;

    /**
     * Parses raw access transformer entry from string
     *
     * @param rawAtEntry Raw AT entry line
     */
    public AccessTransformEntry(@NonNull String rawAtEntry) {
        String[] atEntry = rawAtEntry.split("\\s+");
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

    private AccessTransformEntry(@NonNull AccessLevel accessLevel, @NonNull List<Modifier.ModifierEntry> modifiers, @NonNull String className, String descriptor, boolean method, boolean classAt) {
        this.accessLevel = accessLevel;
        this.modifiers = modifiers;
        this.className = className;
        this.descriptor = descriptor;
        this.method = method;
        this.classAt = classAt;
    }

    /**
     * Gets {@link AccessTransformEntry}'s {@link AccessLevel}
     *
     * @return Instance of {@link AccessLevel}
     */
    @NonNull
    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Gets {@link AccessTransformEntry}'s {@link Modifier.ModifierEntry} list
     *
     * @return List of {@link Modifier.ModifierEntry}
     */
    @NonNull
    public List<Modifier.ModifierEntry> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    /**
     * Gets target class name (method/field owner or class itself)
     *
     * @return Target class name
     */
    @NonNull
    public String getClassName() {
        return className;
    }

    /**
     * Gets target field/method descriptor (field does not have <pre>()V</pre>-like descriptor though)
     *
     * @return Target method descriptor or null
     */
    @Nullable
    public String getDescriptor() {
        return descriptor;
    }

    /**
     * Returns whether {@link AccessTransformEntry} targets method or not
     *
     * @return Whether {@link AccessTransformEntry} targets method or not
     */
    public boolean isMethod() {
        return method;
    }

    /**
     * Returns whether {@link AccessTransformEntry} targets class or not
     *
     * @return Whether {@link AccessTransformEntry} targets class or not
     */
    public boolean isClassAt() {
        return classAt;
    }

    /**
     * Merges this {@link AccessTransformEntry} into other {@link AccessTransformEntry}
     *
     * @param other Other {@link AccessTransformEntry} to merge with
     * @return New {@link AccessTransformEntry}
     */
    @NonNull
    public AccessTransformEntry merge(@NonNull AccessTransformEntry other) {
        if(this.method != other.method)
            throw new IllegalArgumentException("Both AccessTransformEntries must target a method");
        if(this.classAt != other.classAt)
            throw new IllegalArgumentException("Both AccessTransformEntries must target a class");
        if(!this.className.equals(other.className))
            throw new IllegalArgumentException("Both AccessTransformEntries must target same class");
        if(!Objects.equals(this.descriptor, other.descriptor))
            throw new IllegalArgumentException("Both AccessTransformEntries must target same descriptor");

        AccessLevel newAccessLevel = this.accessLevel.ordinal() > other.accessLevel.ordinal()  ? this.accessLevel : other.accessLevel;

        Map<Modifier, Modifier.ModifierEntry> newModifiers = new HashMap<>();
        for (Modifier.ModifierEntry modifierEntry : this.modifiers) {
            newModifiers.put(modifierEntry.getModifier(), modifierEntry);
        }

        for (Modifier.ModifierEntry modifierEntry : other.modifiers) {
            newModifiers.put(modifierEntry.getModifier(), modifierEntry);
        }

        return new AccessTransformEntry(newAccessLevel, new ArrayList<>(newModifiers.values()), this.className, this.descriptor, this.method, this.classAt);
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
        return Objects.equals(descriptor, that.descriptor);
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

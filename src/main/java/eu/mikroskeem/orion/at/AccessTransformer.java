package eu.mikroskeem.orion.at;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Access transformer
 *
 * @author Mark Vainomaa
 */
public final class AccessTransformer {
    private final List<AccessTransformEntry> atEntries = new ArrayList<>();

    /**
     * Gets list of {@link AccessTransformEntry} objects
     *
     * @return List of {@link AccessTransformEntry} objects
     */
    public List<AccessTransformEntry> getAtEntries() {
        return atEntries;
    }

    /**
     * Loads Access transformer entries from {@link BufferedReader} instance
     *
     * @param reader {@link BufferedReader} instance
     * @throws IOException if {@link BufferedReader} throws {@link IOException}
     */
    public void loadAccessTransformers(BufferedReader reader) throws IOException {
        reader.lines().map(this::processLine).filter(Objects::nonNull).forEach(atEntries::add);
    }

    /**
     * Loads Access transformer entries from {@link BufferedReader} instance
     *
     * @param inputStream {@link InputStream} instance
     * @throws IOException if {@link InputStream} throws {@link IOException}
     */
    public void loadAccessTransformers(InputStream inputStream) throws IOException {
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            loadAccessTransformers(bufferedReader);
        }
    }

    /**
     * Loads access transformer entry from string (must be single line)
     * @param atLine Raw AT entry
     */
    public void loadAccessTransformer(String atLine) {
        atEntries.add(Objects.requireNonNull(processLine(atLine), "Invalid AT entry: " + atLine));
    }

    /**
     * Transforms class according to entries in {@link AccessTransformer#atEntries}
     *
     * @param source Class bytes
     * @return Transformed class bytes
     */
    public byte[] transformClass(byte[] source) {
        ClassReader classReader = new ClassReader(source);
        ClassWriter classWriter = new ClassWriter(0);
        AccessTransformerVisitor at = new AccessTransformerVisitor(atEntries, classWriter);
        classReader.accept(at, 0);
        return classWriter.toByteArray();
    }

    /**
     * Processes Access transformer line
     *
     * @param rawAtEntry Access transformer file line
     * @return Parsed line or null, either if it was blank line or comment line
     */
    @Nullable
    private AccessTransformEntry processLine(String rawAtEntry) {
        /* It is a comment! */
        if(rawAtEntry.startsWith("#"))
            return null;

        /* It has a comment! */
        int hashPos = rawAtEntry.indexOf('#');
        if(hashPos != -1)
            rawAtEntry = rawAtEntry.substring(0, hashPos);

        /* It must be trimmed! */
        rawAtEntry = rawAtEntry.trim();

        /* It must not be empty! */
        if(rawAtEntry.isEmpty())
            return null;

        /* Parse line */
        return new AccessTransformEntry(rawAtEntry);
    }
}

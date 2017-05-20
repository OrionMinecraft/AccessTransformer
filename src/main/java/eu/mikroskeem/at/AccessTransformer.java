package eu.mikroskeem.at;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Access transformer
 *
 * @author Mark Vainomaa
 */
public class AccessTransformer {
    private final List<AccessTransformEntry> atEntries = new ArrayList<>();

    public void loadAccessTransformers(BufferedReader reader) throws IOException {
        reader.lines().forEach(line -> {
            /* It is a comment! */
            if(line.startsWith("#")) {
                return;
            }

            /* It has a comment! */
            int hashPos = line.indexOf('#');
            if(hashPos != -1) {
                line = line.substring(0, hashPos);
            }

            /* It must be trimmed! */
            line = line.trim();

            /* It must not be empty! */
            if(line.isEmpty()) {
                return;
            }

            /* And now do something with it... */
            atEntries.add(new AccessTransformEntry(line));
        });
        reader.close();
    }

    public byte[] transformClass(byte[] source) {
        ClassReader classReader = new ClassReader(source);
        ClassWriter classWriter = new ClassWriter(0);
        AccessTransformerVisitor at = new AccessTransformerVisitor(atEntries, classWriter);
        classReader.accept(at, 0);
        return classWriter.toByteArray();
    }
}

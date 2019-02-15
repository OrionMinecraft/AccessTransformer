package eu.mikroskeem.test.orion.at;

import eu.mikroskeem.orion.at.AccessTransformer;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import eu.mikroskeem.shuriken.instrumentation.ClassTools;
import eu.mikroskeem.shuriken.reflect.ClassWrapper;
import eu.mikroskeem.shuriken.reflect.FieldWrapper;
import eu.mikroskeem.shuriken.reflect.Reflect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author Mark Vainomaa
 */
public class AccessTransformerTest {
    @Test
    @SuppressWarnings("ConstantConditions")
    public void testFieldAccessTransformer() throws Exception {
        Class<?> newClass = transform("test_field_at.cfg", TestClass1.class);
        ClassWrapper<?> cw = Reflect.wrapClass(newClass);

        /* Do assertions */
        int aModifiers = cw.getField("a", String.class).get().getField().getModifiers();
        int bModifiers = cw.getField("b", String.class).get().getField().getModifiers();
        Assertions.assertTrue(Modifier.isPublic(aModifiers), "Field a must be public!");
        Assertions.assertTrue(Modifier.isPublic(bModifiers), "Field b must be public!");
    }

    @Test
    public void testMethodAccessTransformer() throws Exception {
        Class<?> newClass = transform("test_method_at.cfg", TestClass1.class);

        /* Do assertions */
        Assertions.assertTrue(Modifier.isPublic(newClass.getConstructor(long.class).getModifiers()),
                "Constructor with long as parameter should be public!");
        Assertions.assertTrue(Modifier.isPublic(newClass.getDeclaredMethod("h", String.class).getModifiers()),
                "Method h should be public!");
    }

    @Test
    public void testMethodFinalRemoveAccessTransformer() throws Exception {
        Class<?> newClass = transform("test_method_final_remove_at.cfg", TestClass3.class);

        /* Do assertions */
        Assertions.assertFalse(Modifier.isFinal(newClass.getDeclaredMethod("h", String.class).getModifiers()),
                "Method h should not be final!");
    }

    @Test
    public void testWildcardFinalRemoveAccessTransformer() throws Exception {
        Class<?> newClass = transform("test_wildcard_final_remove_at.cfg", TestClass1.class);

        /* Do assertions */
        List<FieldWrapper<?>> fields = Reflect.wrapClass(newClass).getFields();
        fields.forEach(fieldWrapper -> {
            Assertions.assertTrue(Modifier.isPublic(fieldWrapper.getField().getModifiers()),
                    String.format("Field %s should be public", fieldWrapper.getField().getName()));
            Assertions.assertFalse(Modifier.isFinal(fieldWrapper.getField().getModifiers()),
                    String.format("Field %s should not be final", fieldWrapper.getField().getName()));
        });
    }

    @Test
    public void testWildcardMethodFinalRemoveAccessTransformer() throws Exception {
        Class<?> newClass = transform("test_method_wildcard_final_remove_at.cfg", TestClass3.class);

        /* Do assertions */
        Assertions.assertFalse(Modifier.isFinal(newClass.getDeclaredMethod("h", String.class).getModifiers()),
                "Method h should not be final!");
    }

    @Test
    public void testClassAccessTransformer() throws Exception {
        Class<?> newClass = transform("test_class_at.cfg", TestClass2.class);

        Assertions.assertFalse(Modifier.isFinal(newClass.getModifiers()), "Class shouldn't be final after transform");
        Assertions.assertTrue(Modifier.isPublic(newClass.getModifiers()), "Class should be public after transform");
    }

    @Test
    public void testInnerClassAccessTransformer() throws Exception {
        Class<?> newClass1 = transform("test_inner_class_at.cfg", Class.forName("eu.mikroskeem.test.orion.at.TestClass4$InnerClass1", false, this.getClass().getClassLoader()));
        Class<?> newClass2 = transform("test_inner_class_at.cfg", Class.forName("eu.mikroskeem.test.orion.at.TestClass4$InnerClass2", false, this.getClass().getClassLoader()));
        Class<?> baseClass = transform("test_inner_class_at.cfg", TestClass4.class);

        Assertions.assertTrue(Modifier.isPublic(newClass1.getModifiers()), "Inner class 1 should be public after transform");
        Assertions.assertTrue(Modifier.isPublic(newClass2.getModifiers()), "Inner class 2 should be public after transform");
        Assertions.assertFalse(Modifier.isFinal(newClass2.getModifiers()), "Inner class 2 shouldn't be final after transform");
        //Assertions.assertEquals(baseClass.getDeclaredClasses()[0], newClass1);
        //Assertions.assertEquals(baseClass.getDeclaredClasses()[1], newClass2);
    }

    /* Utils */
    private AccessTransformer setupAt(String file) throws IOException {
        AccessTransformer at = new AccessTransformer();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + file)));
        at.loadAccessTransformers(br);
        return at;
    }

    private Class<?> transformAndLoad(AccessTransformer at, Class<?> clazz) throws IOException {
        /* Read class and transform it */
        byte[] clazzData = at.transformClass(getClass(clazz));

        /* Load class */
        URLClassLoader newUcl = URLClassLoader.newInstance(new URL[0], null);
        return ClassLoaderTools.defineClass(newUcl, clazz.getName(), clazzData);
    }

    private Class<?> transform(String atFile, Class<?> clazz) throws IOException {
        return transformAndLoad(setupAt(atFile), clazz);
    }

    private byte[] getClass(Class<?> clazz) throws IOException {
        URLClassLoader ucl = (URLClassLoader)this.getClass().getClassLoader();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(InputStream is = ucl.getResourceAsStream(ClassTools.unqualifyName(clazz) + ".class")) {
            while(is.available() != 0) baos.write(is.read());
        }
        return baos.toByteArray();
    }

    private void dumpClass(byte[] rawClass) {
        ClassReader cr = new ClassReader(rawClass);
        cr.accept(new TraceClassVisitor(null, new PrintWriter(System.out)), 0);
    }
}

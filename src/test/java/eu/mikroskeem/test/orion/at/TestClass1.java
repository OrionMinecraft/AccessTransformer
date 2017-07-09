package eu.mikroskeem.test.orion.at;

/**
 * @author Mark Vainomaa
 */
public class TestClass1 {
    public TestClass1(int a) {}
    TestClass1(long b) {}
    private TestClass1(char c) {}

    /* Fields */
    private String a;
    protected String b;
    String c;
    public String d;

    private final String finalA = "";
    public final String staticFinalB = "";

    /* Methods */
    private void a() {}
    protected void b() {}
    void c() {}
    public void d() {}
    public final void e() {}
    public static void f() {}
    private String g() { return null; }
    private void h(String a) {}

    /* Static block '<clinit>' */
    static {
        f();
    }
}

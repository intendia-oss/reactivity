package sample.nested.client;

import com.google.gwt.junit.client.GWTTestCase;

public class SandboxGwtTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "sample.nested.Sample";
    }

    public void testSandbox() {
        assertTrue(true);
    }
}

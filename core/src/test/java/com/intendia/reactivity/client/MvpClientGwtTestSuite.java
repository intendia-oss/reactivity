package com.intendia.reactivity.client;

import com.google.gwt.junit.tools.GWTTestSuite;
import junit.framework.Test;
import junit.framework.TestCase;

public class MvpClientGwtTestSuite extends TestCase {
    public static Test suite() {
        GWTTestSuite suite = new GWTTestSuite("All the GWT test cases.");
        suite.addTestSuite(ParameterTokenFormatterGwtTestInSuite.class);
        suite.addTestSuite(ReactivityGwtTestInSuite.class);
        return suite;
    }
}

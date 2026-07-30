/*******************************************************************************
 * Copyright (c) 2016, 2026 Red Hat Inc and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Red Hat Inc
 *******************************************************************************/
package org.eclipse.kura.camel.runner;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class ScriptRunnerTest {

    /**
     * These tests need a JSR-223 "JavaScript" engine. Nashorn was removed from the JDK in Java 15,
     * so on the Java 21 runtime this add-on targets there is none unless one is added explicitly.
     * The feature itself is already documented as Java-8-only in the metatype of
     * org.eclipse.kura.camel.xml.
     */
    @Before
    public void requireJavaScriptEngine() {
        Assume.assumeNotNull(new ScriptEngineManager().getEngineByName("JavaScript"));
    }

    /**
     * Test a simple call
     */
    @Test
    public void testScript1() throws ScriptException {
        final ScriptRunner runner = ScriptRunner.create(null, "JavaScript", "42;");

        final Object result = runner.run();

        Assert.assertTrue(result instanceof Number);
        Assert.assertEquals(42.0, ((Number) result).doubleValue(),0.001);
    }

    /**
     * Test a call with arguments
     */
    @Test
    public void testScript2() throws ScriptException {
        final ScriptRunner runner = ScriptRunner.create(null, "JavaScript", "foo + 'bar';");

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("foo", "bar");
        final Object result = runner.run(bindings);

        Assert.assertEquals("barbar", result);
    }
}

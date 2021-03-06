/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.javadude.t2bus;

import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test case for {@link EventHandler}.
 *
 * @author Cliff Biffle
 */
public class EventHandlerTest {

    private static final Object FIXTURE_ARGUMENT = new Object();

    private boolean methodCalled;
    private Object methodArgument;

    @Before
    public void setUp() throws Exception {
        methodCalled = false;
        methodArgument = null;
    }

    /**
     * Checks that a no-frills, no-issues method call is properly executed.
     *
     * @throws Exception if the aforementioned proper execution is not to be had.
     */
    @Test
    public void testBasicMethodCall() throws Exception {
        Method method = getRecordingMethod();

        EventHandler handler = new EventHandler(this, method, false);

        handler.handleEvent(FIXTURE_ARGUMENT);

        assertThat("Handler must call provided method.", methodCalled);
        assertThat("Handler argument must be *exactly* the provided object.",
                methodArgument == FIXTURE_ARGUMENT);
    }

    /**
     * Checks that EventHandler's constructor disallows null methods.
     */
    @Test
    public void testRejectionOfNullMethods() {
        try {
            new EventHandler(this, null);
            fail("EventHandler must immediately reject null methods.");
        } catch (NullPointerException e) {
            // Hooray!
        }
    }

    /**
     * Checks that EventHandler's constructor disallows null targets.
     */
    @Test
    public void testRejectionOfNullTargets() {
        Method method = getRecordingMethod();
        try {
            new EventHandler(null, method);
            fail("EventHandler must immediately reject null targets.");
        } catch (NullPointerException e) {
            // Huzzah!
        }
    }

    @Test
    public void testExceptionWrapping() throws VetoException {
        Method method = getExceptionThrowingMethod();
        EventHandler handler = new EventHandler(this, method);

        try {
            handler.handleEvent(new Object());
            fail("Handlers whose methods throw must throw InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertThat("Expected exception must be wrapped.",
                    e.getCause() instanceof IntentionalException);
        }
    }

    @Test(expected = JudgmentError.class)
    public void testErrorPassthrough() throws InvocationTargetException, VetoException {
        Method method = getErrorThrowingMethod();
        EventHandler handler = new EventHandler(this, method);

        handler.handleEvent(new Object());
        fail("Handlers whose methods throw Errors must rethrow them");
    }

    /**
     * Gets a reference to {@link #recordingMethod(Object)}.
     *
     * @return a Method wrapping {@link #recordingMethod(Object)}.
     * @throws IllegalStateException if executed in a context where reflection is
     *                               unavailable.
     * @throws AssertionError        if something odd has happened to
     *                               {@link #recordingMethod(Object)}.
     */
    private Method getRecordingMethod() {
        Method method;
        try {
            method = getClass().getMethod("recordingMethod", Object.class);
        } catch (SecurityException e) {
            throw new IllegalStateException("This test needs access to reflection.");
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Someone changed EventHandlerTest#recordingMethod's visibility, " +
                            "signature, or removed it entirely.  (Must be public.)");
        }
        return method;
    }

    /**
     * Gets a reference to {@link #exceptionThrowingMethod(Object)}.
     *
     * @return a Method wrapping {@link #exceptionThrowingMethod(Object)}.
     * @throws IllegalStateException if executed in a context where reflection is
     *                               unavailable.
     * @throws AssertionError        if something odd has happened to
     *                               {@link #exceptionThrowingMethod(Object)}.
     */
    private Method getExceptionThrowingMethod() {
        Method method;
        try {
            method = getClass().getMethod("exceptionThrowingMethod", Object.class);
        } catch (SecurityException e) {
            throw new IllegalStateException("This test needs access to reflection.");
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Someone changed EventHandlerTest#exceptionThrowingMethod's " +
                            "visibility, signature, or removed it entirely.  (Must be public.)");
        }
        return method;
    }

    /**
     * Gets a reference to {@link #errorThrowingMethod(Object)}.
     *
     * @return a Method wrapping {@link #errorThrowingMethod(Object)}.
     * @throws IllegalStateException if executed in a context where reflection is
     *                               unavailable.
     * @throws AssertionError        if something odd has happened to
     *                               {@link #errorThrowingMethod(Object)}.
     */
    private Method getErrorThrowingMethod() {
        Method method;
        try {
            method = getClass().getMethod("errorThrowingMethod", Object.class);
        } catch (SecurityException e) {
            throw new IllegalStateException("This test needs access to reflection.");
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                    "Someone changed EventHandlerTest#errorThrowingMethod's " +
                            "visibility, signature, or removed it entirely.  (Must be public.)");
        }
        return method;
    }

    /**
     * Records the provided object in {@link #methodArgument} and sets
     * {@link #methodCalled}.  This method is called reflectively by EventHandler
     * during tests, and must remain public.
     *
     * @param arg argument to record.
     */
    public void recordingMethod(Object arg) {
        if (methodCalled == true) {
            throw new IllegalStateException("Method called more than once.");
        }
        methodCalled = true;
        methodArgument = arg;
    }

    public void exceptionThrowingMethod(Object arg) throws Exception {
        throw new IntentionalException();
    }

    /**
     * Local exception subclass to check variety of exception thrown.
     */
    class IntentionalException extends Exception {
        private static final long serialVersionUID = -2500191180248181379L;
    }

    public void errorThrowingMethod(Object arg) {
        throw new JudgmentError();
    }

    /**
     * Local Error subclass to check variety of error thrown.
     */
    class JudgmentError extends Error {
        private static final long serialVersionUID = 634248373797713373L;
    }
}

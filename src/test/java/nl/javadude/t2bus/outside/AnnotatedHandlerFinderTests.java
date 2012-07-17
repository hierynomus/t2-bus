/*
 * Copyright (C) 2012 The Guava Authors
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

package nl.javadude.t2bus.outside;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import nl.javadude.t2bus.EventBus;
import nl.javadude.t2bus.Subscribe;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Test that EventBus finds the correct handlers.
 *
 * This test must be outside the c.g.c.eventbus package to test correctly.
 * @author Louis Wasserman
 */
public class AnnotatedHandlerFinderTests {

  private static final Object EVENT = new Object();

  abstract static class AbstractEventBusTest<H> extends TestCase {
    abstract H createHandler();

    private H handler;

    H getHandler() {
      return handler;
    }

    @Override
    protected void setUp() throws Exception {
      handler = createHandler();
      EventBus bus = new EventBus();
      bus.register(handler);
      bus.post(EVENT);
    }

    @Override
    protected void tearDown() throws Exception {
      handler = null;
    }
  }

  /*
   * We break the tests up based on whether they are annotated or abstract in the superclass.
   */
  public static class BaseHandlerFinderTest extends
      AbstractEventBusTest<BaseHandlerFinderTest.Handler> {
    static class Handler {
      final List<Object> nonSubscriberEvents = Lists.newArrayList();
      final List<Object> subscriberEvents = Lists.newArrayList();

      public void notASubscriber(Object o) {
        nonSubscriberEvents.add(o);
      }

      @Subscribe
      public void subscriber(Object o) {
        subscriberEvents.add(o);
      }
    }

    public void testNonSubscriber() {
      assertThat(getHandler().nonSubscriberEvents, empty());
    }

    public void testSubscriber() {
      assertThat(getHandler().subscriberEvents, hasItems(EVENT));
    }

    @Override
    Handler createHandler() {
      return new Handler();
    }
  }

  public static class AnnotatedAndAbstractInSuperclassTest extends
      AbstractEventBusTest<AnnotatedAndAbstractInSuperclassTest.SubClass> {
    abstract static class SuperClass {
      @Subscribe
      public abstract void overriddenAndAnnotatedInSubclass(Object o);

      @Subscribe
      public abstract void overriddenInSubclass(Object o);
    }

    static class SubClass extends SuperClass {
      final List<Object> overriddenAndAnnotatedInSubclassEvents = Lists.newArrayList();
      final List<Object> overriddenInSubclassEvents = Lists.newArrayList();

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }

      @Override
      public void overriddenInSubclass(Object o) {
        overriddenInSubclassEvents.add(o);
      }
    }

    public void testOverriddenAndAnnotatedInSubclass() {
      assertThat(getHandler().overriddenAndAnnotatedInSubclassEvents, hasItems(EVENT));
    }

    public void testOverriddenNotAnnotatedInSubclass() {
      assertThat(getHandler().overriddenInSubclassEvents, hasItems(EVENT));
    }

    @Override
    SubClass createHandler() {
      return new SubClass();
    }
  }

  public static class AnnotatedNotAbstractInSuperclassTest extends
      AbstractEventBusTest<AnnotatedNotAbstractInSuperclassTest.SubClass> {
    static class SuperClass {
      final List<Object> notOverriddenInSubclassEvents = Lists.newArrayList();
      final List<Object> overriddenNotAnnotatedInSubclassEvents = Lists.newArrayList();
      final List<Object> overriddenAndAnnotatedInSubclassEvents = Lists.newArrayList();
      final List<Object> differentlyOverriddenNotAnnotatedInSubclassBadEvents = Lists
          .newArrayList();
      final List<Object> differentlyOverriddenAnnotatedInSubclassBadEvents = Lists.newArrayList();

      @Subscribe
      public void notOverriddenInSubclass(Object o) {
        notOverriddenInSubclassEvents.add(o);
      }

      @Subscribe
      public void overriddenNotAnnotatedInSubclass(Object o) {
        overriddenNotAnnotatedInSubclassEvents.add(o);
      }

      @Subscribe
      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }

      @Subscribe
      public void differentlyOverriddenNotAnnotatedInSubclass(Object o) {
        // the subclass overrides this and does *not* call super.dONAIS(o)
        differentlyOverriddenNotAnnotatedInSubclassBadEvents.add(o);
      }

      @Subscribe
      public void differentlyOverriddenAnnotatedInSubclass(Object o) {
        // the subclass overrides this and does *not* call super.dOAIS(o)
        differentlyOverriddenAnnotatedInSubclassBadEvents.add(o);
      }
    }

    static class SubClass extends SuperClass {
      final List<Object> differentlyOverriddenNotAnnotatedInSubclassGoodEvents = Lists
          .newArrayList();
      final List<Object> differentlyOverriddenAnnotatedInSubclassGoodEvents = Lists.newArrayList();

      @Override
      public void overriddenNotAnnotatedInSubclass(Object o) {
        super.overriddenNotAnnotatedInSubclass(o);
      }

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        super.overriddenAndAnnotatedInSubclass(o);
      }

      @Override
      public void differentlyOverriddenNotAnnotatedInSubclass(Object o) {
        differentlyOverriddenNotAnnotatedInSubclassGoodEvents.add(o);
      }

      @Subscribe
      @Override
      public void differentlyOverriddenAnnotatedInSubclass(Object o) {
        differentlyOverriddenAnnotatedInSubclassGoodEvents.add(o);
      }
    }

    public void testNotOverriddenInSubclass() {
      assertThat(getHandler().notOverriddenInSubclassEvents, hasItems(EVENT));
    }

    public void testOverriddenNotAnnotatedInSubclass() {
      assertThat(getHandler().overriddenNotAnnotatedInSubclassEvents, hasItems(EVENT));
    }

    public void testDifferentlyOverriddenNotAnnotatedInSubclass() {
      assertThat(getHandler().differentlyOverriddenNotAnnotatedInSubclassGoodEvents, hasItems(EVENT));
      assertThat(getHandler().differentlyOverriddenNotAnnotatedInSubclassBadEvents, empty());
    }

    public void testOverriddenAndAnnotatedInSubclass() {
      assertThat(getHandler().overriddenAndAnnotatedInSubclassEvents, hasItems(EVENT));
    }

    public void testDifferentlyOverriddenAndAnnotatedInSubclass() {
      assertThat(getHandler().differentlyOverriddenAnnotatedInSubclassGoodEvents, hasItems(EVENT));
      assertThat(getHandler().differentlyOverriddenAnnotatedInSubclassBadEvents, empty());
    }

    @Override
    SubClass createHandler() {
      return new SubClass();
    }
  }

  public static class AbstractNotAnnotatedInSuperclassTest extends
      AbstractEventBusTest<AbstractNotAnnotatedInSuperclassTest.SubClass> {
    abstract static class SuperClass {
      public abstract void overriddenInSubclassNowhereAnnotated(Object o);

      public abstract void overriddenAndAnnotatedInSubclass(Object o);
    }

    static class SubClass extends SuperClass {
      final List<Object> overriddenInSubclassNowhereAnnotatedEvents = Lists.newArrayList();
      final List<Object> overriddenAndAnnotatedInSubclassEvents = Lists.newArrayList();

      @Override
      public void overriddenInSubclassNowhereAnnotated(Object o) {
        overriddenInSubclassNowhereAnnotatedEvents.add(o);
      }

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }
    }

    public void testOverriddenAndAnnotatedInSubclass() {
      assertThat(getHandler().overriddenAndAnnotatedInSubclassEvents, hasItems(EVENT));
    }

    public void testOverriddenInSubclassNowhereAnnotated() {
      assertThat(getHandler().overriddenInSubclassNowhereAnnotatedEvents, empty());
    }

    @Override
    SubClass createHandler() {
      return new SubClass();
    }
  }

  public static class NeitherAbstractNorAnnotatedInSuperclassTest extends
      AbstractEventBusTest<NeitherAbstractNorAnnotatedInSuperclassTest.SubClass> {
    static class SuperClass {
      final List<Object> neitherOverriddenNorAnnotatedEvents = Lists.newArrayList();
      final List<Object> overriddenInSubclassNowhereAnnotatedEvents = Lists.newArrayList();
      final List<Object> overriddenAndAnnotatedInSubclassEvents = Lists.newArrayList();

      public void neitherOverriddenNorAnnotated(Object o) {
        neitherOverriddenNorAnnotatedEvents.add(o);
      }

      public void overriddenInSubclassNowhereAnnotated(Object o) {
        overriddenInSubclassNowhereAnnotatedEvents.add(o);
      }

      public void overriddenAndAnnotatedInSubclass(Object o) {
        overriddenAndAnnotatedInSubclassEvents.add(o);
      }
    }

    static class SubClass extends SuperClass {
      @Override
      public void overriddenInSubclassNowhereAnnotated(Object o) {
        super.overriddenInSubclassNowhereAnnotated(o);
      }

      @Subscribe
      @Override
      public void overriddenAndAnnotatedInSubclass(Object o) {
        super.overriddenAndAnnotatedInSubclass(o);
      }
    }

    public void testNeitherOverriddenNorAnnotated() {
      assertThat(getHandler().neitherOverriddenNorAnnotatedEvents, empty());
    }

    public void testOverriddenInSubclassNowhereAnnotated() {
      assertThat(getHandler().overriddenInSubclassNowhereAnnotatedEvents, empty());
    }

    public void testOverriddenAndAnnotatedInSubclass() {
      assertThat(getHandler().overriddenAndAnnotatedInSubclassEvents, hasItems(EVENT));
    }

    @Override
    SubClass createHandler() {
      return new SubClass();
    }
  }

  public static class DeepInterfaceTest extends
      AbstractEventBusTest<DeepInterfaceTest.HandlerClass> {
    interface Interface1 {
      @Subscribe
      void annotatedIn1(Object o);

      @Subscribe
      void annotatedIn1And2(Object o);

      @Subscribe
      void annotatedIn1And2AndClass(Object o);

      void declaredIn1AnnotatedIn2(Object o);

      void declaredIn1AnnotatedInClass(Object o);

      void nowhereAnnotated(Object o);
    }

    interface Interface2 extends Interface1 {
      @Override
      @Subscribe
      void declaredIn1AnnotatedIn2(Object o);

      @Override
      @Subscribe
      void annotatedIn1And2(Object o);

      @Override
      @Subscribe
      void annotatedIn1And2AndClass(Object o);

      void declaredIn2AnnotatedInClass(Object o);

      @Subscribe
      void annotatedIn2(Object o);
    }

    static class HandlerClass implements Interface2 {
      final List<Object> annotatedIn1Events = Lists.newArrayList();
      final List<Object> annotatedIn1And2Events = Lists.newArrayList();
      final List<Object> annotatedIn1And2AndClassEvents = Lists.newArrayList();
      final List<Object> declaredIn1AnnotatedIn2Events = Lists.newArrayList();
      final List<Object> declaredIn1AnnotatedInClassEvents = Lists.newArrayList();
      final List<Object> declaredIn2AnnotatedInClassEvents = Lists.newArrayList();
      final List<Object> annotatedIn2Events = Lists.newArrayList();
      final List<Object> nowhereAnnotatedEvents = Lists.newArrayList();

      @Override
      public void annotatedIn1(Object o) {
        annotatedIn1Events.add(o);
      }

      @Subscribe
      @Override
      public void declaredIn1AnnotatedInClass(Object o) {
        declaredIn1AnnotatedInClassEvents.add(o);
      }

      @Override
      public void declaredIn1AnnotatedIn2(Object o) {
        declaredIn1AnnotatedIn2Events.add(o);
      }

      @Override
      public void annotatedIn1And2(Object o) {
        annotatedIn1And2Events.add(o);
      }

      @Subscribe
      @Override
      public void annotatedIn1And2AndClass(Object o) {
        annotatedIn1And2AndClassEvents.add(o);
      }

      @Subscribe
      @Override
      public void declaredIn2AnnotatedInClass(Object o) {
        declaredIn2AnnotatedInClassEvents.add(o);
      }

      @Override
      public void annotatedIn2(Object o) {
        annotatedIn2Events.add(o);
      }

      @Override
      public void nowhereAnnotated(Object o) {
        nowhereAnnotatedEvents.add(o);
      }
    }

    public void testAnnotatedIn1() {
      assertThat(getHandler().annotatedIn1Events, hasItems(EVENT));
    }

    public void testAnnotatedIn2() {
      assertThat(getHandler().annotatedIn2Events, hasItems(EVENT));
    }

    public void testAnnotatedIn1And2() {
      assertThat(getHandler().annotatedIn1And2Events, hasItems(EVENT));
    }

    public void testAnnotatedIn1And2AndClass() {
      assertThat(getHandler().annotatedIn1And2AndClassEvents, hasItems(EVENT));
    }

    public void testDeclaredIn1AnnotatedIn2() {
      assertThat(getHandler().declaredIn1AnnotatedIn2Events, hasItems(EVENT));
    }

    public void testDeclaredIn1AnnotatedInClass() {
      assertThat(getHandler().declaredIn1AnnotatedInClassEvents, hasItems(EVENT));
    }

    public void testDeclaredIn2AnnotatedInClass() {
      assertThat(getHandler().declaredIn2AnnotatedInClassEvents, hasItems(EVENT));
    }

    public void testNowhereAnnotated() {
      assertThat(getHandler().nowhereAnnotatedEvents, empty());
    }

    @Override
    HandlerClass createHandler() {
      return new HandlerClass();
    }
  }
}

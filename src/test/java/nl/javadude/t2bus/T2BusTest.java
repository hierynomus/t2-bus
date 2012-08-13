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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link com.google.common.eventbus.EventBus}.
 *
 * @author Cliff Biffle
 */
public class T2BusTest {
    private static final String EVENT = "Hello";
    private static final String BUS_IDENTIFIER = "test-bus";

    private T2Bus bus;

    @Before
    public void setUp() throws Exception {
        bus = new T2Bus(BUS_IDENTIFIER);
    }

    @Test
    public void basicCatcherDistribution() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);

        Set<EventHandler> wrappers = bus.getHandlersForEventType(String.class);
        assertThat("Should have at least one method registered.", wrappers, notNullValue());
        assertThat("One method should be registered.", wrappers, hasSize(1));

        bus.post(EVENT);

        List<String> events = catcher.getEvents();
        assertThat("Only one event should be delivered.", events, hasSize(1));
        assertThat("Correct string should be delivered.", events.get(0), equalTo(EVENT));
    }

    /**
     * Tests that events are distributed to any subscribers to their type or any
     * supertype, including interfaces and superclasses.
     * <p/>
     * Also checks delivery ordering in such cases.
     */
    @Test
    public void polymorphicDistribution() {
        // Three catchers for related types String, Object, and Comparable<?>.
        // String isa Object
        // String isa Comparable<?>
        // Comparable<?> isa Object
        StringCatcher stringCatcher = new StringCatcher();

        final List<Object> objectEvents = Lists.newArrayList();
        Object objCatcher = new Object() {
            @SuppressWarnings("unused")
            @Subscribe
            public void eat(Object food) {
                objectEvents.add(food);
            }
        };

        final List<Comparable<?>> compEvents = Lists.newArrayList();
        Object compCatcher = new Object() {
            @SuppressWarnings("unused")
            @Subscribe
            public void eat(Comparable<?> food) {
                compEvents.add(food);
            }
        };
        bus.register(stringCatcher);
        bus.register(objCatcher);
        bus.register(compCatcher);

        // Two additional event types: Object and Comparable<?> (played by Integer)
        final Object OBJ_EVENT = new Object();
        final Object COMP_EVENT = new Integer(6);

        bus.post(EVENT);
        bus.post(OBJ_EVENT);
        bus.post(COMP_EVENT);

        // Check the StringCatcher...
        List<String> stringEvents = stringCatcher.getEvents();
        assertThat("Only one String should be delivered.", stringEvents, hasSize(1));
        assertThat("Correct string should be delivered.", stringEvents.get(0), equalTo(EVENT));

        // Check the Catcher<Object>...
        assertThat("Three Objects should be delivered.", objectEvents, hasSize(3));
        assertThat("String fixture must be first object delivered.", (String) objectEvents.get(0), equalTo(EVENT));
        assertThat("Object fixture must be second object delivered.", objectEvents.get(1), equalTo(OBJ_EVENT));
        assertThat("Comparable fixture must be thirdobject delivered.", objectEvents.get(2), equalTo(COMP_EVENT));

        // Check the Catcher<Comparable<?>>...
        assertThat("Two Comparable<?>s should be delivered.", compEvents, hasSize(2));
        assertThat("String fixture must be first comparable delivered.", (String) compEvents.get(0), equalTo(EVENT));
        assertThat("Comparable fixture must be second comparable delivered.", compEvents.get(1), equalTo(COMP_EVENT));
    }

    @Test
    public void deadEventForwarding() {
        GhostCatcher catcher = new GhostCatcher();
        bus.register(catcher);

        // A String -- an event for which noone has registered.
        bus.post(EVENT);

        List<DeadEvent> events = catcher.getEvents();
        assertThat("One dead event should be delivered.", events, hasSize(1));
        assertThat("The dead event should wrap the original event.", (String) events.get(0).getEvent(), equalTo(EVENT));
    }

    @Test
    public void deadEventPosting() {
        GhostCatcher catcher = new GhostCatcher();
        bus.register(catcher);

        bus.post(new DeadEvent(this, EVENT));

        List<DeadEvent> events = catcher.getEvents();
        assertThat("The explicit DeadEvent should be delivered.", events, hasSize(1));
        assertThat("The dead event must not be re-wrapped.", (String) events.get(0).getEvent(), equalTo(EVENT));
    }

    @Test
    public void flattenHierarchy() {
        HierarchyFixture fixture = new HierarchyFixture();
        Set<Class<?>> hierarchy = bus.flattenHierarchy(fixture.getClass());

        assertThat(hierarchy, hasSize(5));
        assertThat(hierarchy, hasItem(Object.class));
        assertThat(hierarchy, hasItem(HierarchyFixtureInterface.class));
        assertThat(hierarchy, hasItem(HierarchyFixtureSubinterface.class));
        assertThat(hierarchy, hasItem(HierarchyFixtureParent.class));
        assertThat(hierarchy, hasItem(HierarchyFixture.class));
    }

    @Test
    public void missingSubscribe() {
        bus.register(new Object());
    }

    @Test
    public void shouldUnregister() {
        StringCatcher catcher1 = new StringCatcher();
        StringCatcher catcher2 = new StringCatcher();
        try {
            bus.unregister(catcher1);
            fail("Attempting to unregister an unregistered object succeeded");
        } catch (IllegalArgumentException expected) {
            // OK.
        }

        bus.register(catcher1);
        bus.post(EVENT);
        bus.register(catcher2);
        bus.post(EVENT);

        List<String> expectedEvents = Lists.newArrayList();
        expectedEvents.add(EVENT);
        expectedEvents.add(EVENT);

        assertThat("Two correct events should be delivered.", catcher1.getEvents(), equalTo(expectedEvents));

        assertThat("One correct event should be delivered.", (ArrayList<String>) catcher2.getEvents(), equalTo(Lists.newArrayList(EVENT)));

        bus.unregister(catcher1);
        bus.post(EVENT);

        assertThat("Shouldn't catch any more events when unregistered.", catcher1.getEvents(), equalTo(expectedEvents));
        assertThat("Two correct events should be delivered.", catcher2.getEvents(), equalTo(expectedEvents));

        try {
            bus.unregister(catcher1);
            fail("Attempting to unregister an unregistered object succeeded");
        } catch (IllegalArgumentException expected) {
            // OK.
        }

        bus.unregister(catcher2);
        bus.post(EVENT);
        assertThat("Shouldn't catch any more events when unregistered.", catcher1.getEvents(), equalTo(expectedEvents));
        assertThat("Shouldn't catch any more events when unregistered.", catcher2.getEvents(), equalTo(expectedEvents));
    }

    @Test
    public void shouldBeAbleToVetoEvent() {
        StringCatcher catcher = new StringCatcher();
        StringVetoer vetoer = new StringVetoer();
        bus.register(catcher);
        bus.register(vetoer);

        bus.post(EVENT);

        assertThat("Should have vetoed event", vetoer.getVetoed(), hasSize(1));
        assertThat("Should have vetoed event", vetoer.getVetoed(), hasItem(EVENT));
        assertThat("Should not have caught vetoed event", catcher.getEvents(), hasSize(0));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotThrowVetoExceptionWhenNotCanVeto() {
        bus.register(new Object() {
            @Subscribe
            public void shouldNotAllowThrowing(Object event) throws VetoException {
            }
        });
    }

    /**
     * A collector for DeadEvents.
     *
     * @author cbiffle
     */
    public static class GhostCatcher {
        private List<DeadEvent> events = Lists.newArrayList();

        @Subscribe
        public void ohNoesIHaveDied(DeadEvent event) {
            events.add(event);
        }

        public List<DeadEvent> getEvents() {
            return events;
        }
    }

    public interface HierarchyFixtureInterface {
        // Exists only for hierarchy mapping; no members.
    }

    public interface HierarchyFixtureSubinterface
            extends HierarchyFixtureInterface {
        // Exists only for hierarchy mapping; no members.
    }

    public static class HierarchyFixtureParent
            implements HierarchyFixtureSubinterface {
        // Exists only for hierarchy mapping; no members.
    }

    public static class HierarchyFixture extends HierarchyFixtureParent {
        // Exists only for hierarchy mapping; no members.
    }

}

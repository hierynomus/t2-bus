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
import org.junit.Test;
import com.google.common.collect.Lists;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Validate that {@link com.google.common.eventbus.EventBus} behaves carefully when listeners publish
 * their own events.
 *
 * @author Jesse Wilson
 */
public class ReentrantEventsTest {

    static final String FIRST = "one";
    static final Double SECOND = 2.0d;

    final T2Bus bus = new T2Bus();

    @Test
    public void shouldNotAllowReentrantEvents() {
        ReentrantEventsHater hater = new ReentrantEventsHater();
        bus.register(hater);

        bus.post(FIRST);

        assertThat("ReentrantEventHater expected 2 events", (ArrayList<Object>) hater.eventsReceived, equalTo(Lists.<Object>newArrayList(FIRST, SECOND)));
    }

    public class ReentrantEventsHater {
        boolean ready = true;
        List<Object> eventsReceived = Lists.newArrayList();

        @Subscribe
        public void listenForStrings(String event) {
            eventsReceived.add(event);
            ready = false;
            try {
                bus.post(SECOND);
            } finally {
                ready = true;
            }
        }

        @Subscribe
        public void listenForDoubles(Double event) {
            assertThat("I received an event when I wasn't ready!", ready, equalTo(true));
            eventsReceived.add(event);
        }
    }

    @Test
    public void testEventOrderingIsPredictable() {
        EventProcessor processor = new EventProcessor();
        bus.register(processor);

        EventRecorder recorder = new EventRecorder();
        bus.register(recorder);

        bus.post(FIRST);

        assertThat("EventRecorder expected events in order", (ArrayList<Object>) recorder.eventsReceived, equalTo(Lists.<Object>newArrayList(FIRST, SECOND)));
    }

    public class EventProcessor {
        @Subscribe
        public void listenForStrings(String event) {
            bus.post(SECOND);
        }
    }

    public class EventRecorder {
        List<Object> eventsReceived = Lists.newArrayList();

        @Subscribe
        public void listenForEverything(Object event) {
            eventsReceived.add(event);
        }
    }
}

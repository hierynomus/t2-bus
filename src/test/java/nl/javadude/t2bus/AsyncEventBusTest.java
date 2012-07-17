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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Test case for {@link com.google.common.eventbus.AsyncEventBus}.
 *
 * @author Cliff Biffle
 */
public class AsyncEventBusTest {
    private static final String EVENT = "Hello";

    /**
     * The executor we use to fake asynchronicity.
     */
    private FakeExecutor executor;
    private AsyncEventBus bus;

    @Before
    public void setup() throws Exception {
        executor = new FakeExecutor();
        bus = new AsyncEventBus(executor);
    }

    @Test
    public void basicDistribution() {
        StringCatcher catcher = new StringCatcher();
        bus.register(catcher);

        // We post the event, but our Executor will not deliver it until instructed.
        bus.post(EVENT);

        List<String> events = catcher.getEvents();
        assertThat("No events should be delivered synchronously.", events, hasSize(0));

        // Now we find the task in our Executor and explicitly activate it.
        List<Runnable> tasks = executor.getTasks();
        assertThat("One event dispatch task should be queued.", tasks, hasSize(1));

        tasks.get(0).run();

        assertThat("One event should be delivered.", events, hasSize(1));
        assertThat("Correct string should be delivered.", events.get(0), equalTo(EVENT));
    }

    @Test
    public void shouldVetoEvents() {
        StringCatcher catcher = new StringCatcher();
        StringVetoer vetoer = new StringVetoer();
        bus.register(catcher);
        bus.register(vetoer);

        bus.post(EVENT);

        executor.getTasks().get(0).run();

        assertThat("No event should be delivered.", catcher.getEvents(), hasSize(0));
        assertThat("One event should be vetoed.", vetoer.getVetoed(), hasSize(1));
        assertThat("Correct string should be vetoed.", vetoer.getVetoed().get(0), equalTo(EVENT));
    }

    /**
     * An {@link java.util.concurrent.Executor} wanna-be that simply records the tasks it's given.
     * Arguably the Worst Executor Ever.
     *
     * @author cbiffle
     */
    public static class FakeExecutor implements Executor {
        List<Runnable> tasks = Lists.newArrayList();

        @Override
        public void execute(Runnable task) {
            tasks.add(task);
        }

        public List<Runnable> getTasks() {
            return tasks;
        }
    }

}

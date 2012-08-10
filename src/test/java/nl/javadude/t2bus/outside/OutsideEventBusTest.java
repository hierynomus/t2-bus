/*
 * Copyright (C) 2011 The Guava Authors
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

import nl.javadude.t2bus.T2Bus;
import nl.javadude.t2bus.Subscribe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test cases for {@code EventBus} that must not be in the same package.
 *
 * @author Louis Wasserman
 */
public class OutsideEventBusTest {

    /*
    * If you do this test from common.eventbus.EventBusTest, it doesn't actually test the behavior.
    * That is, even if exactly the same method works from inside the common.eventbus package tests,
    * it can fail here.
    */
    @Test
    public void anonymous() {
        final AtomicReference<String> holder = new AtomicReference<String>();
        final AtomicInteger deliveries = new AtomicInteger();
        T2Bus bus = new T2Bus();
        bus.register(new Object() {
            @Subscribe
            public void accept(String str) {
                holder.set(str);
                deliveries.incrementAndGet();
            }
        });

        String EVENT = "Hello!";
        bus.post(EVENT);

        assertThat("Only one event should be delivered.", deliveries.get(), equalTo(1));
        assertThat("Correct string should be delivered.", holder.get(), equalTo(EVENT));
    }
}

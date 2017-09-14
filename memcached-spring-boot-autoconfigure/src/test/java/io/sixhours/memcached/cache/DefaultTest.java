/*
 * Copyright 2017 Sixhours.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sixhours.memcached.cache;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.CoreMatchers.isA;

/**
 * Default cache configuration values tests.
 *
 * @author Igor Bolic
 */
public class DefaultTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void thatConstructorThrowsException() throws Exception {
        Constructor constructor = Default.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        thrown.expect(InvocationTargetException.class);
        thrown.expectCause(isA(AssertionError.class));

        constructor.newInstance();
    }

}
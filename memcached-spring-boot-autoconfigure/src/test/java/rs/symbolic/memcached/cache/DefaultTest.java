package rs.symbolic.memcached.cache;

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
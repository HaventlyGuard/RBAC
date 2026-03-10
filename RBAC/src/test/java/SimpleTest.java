import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTest {

    @Test
    public void testSimple() {
        assertTrue(true);
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testString() {
        String str = "hello";
        assertEquals("hello", str);
    }
}
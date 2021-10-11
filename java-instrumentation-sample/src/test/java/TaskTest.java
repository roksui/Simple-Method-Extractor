import com.company.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TaskTest {

    Task task = new Task();

    @Test
    public void addTest() {
        Assertions.assertEquals(4, task.add(2, 2));
    }
}

import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.junit.Test;

public class ClientTest {
    @Test
    public void test() {
        ClientConfigurationBean configuration = new ClientConfigurationBean();
        configuration.setSecret("OASAD834jA97AAQE761==");

        Tracker tracker = new Tracker();
        tracker.setHost("127.0.0.1");
        tracker.setPort(1022);
        tracker.setMaxConnections(5);
        configuration.addTracker(tracker);

        GoDFSClient client = new GoDFSClient(configuration);
        client.start();
    }
}

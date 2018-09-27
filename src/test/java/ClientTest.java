import com.alibaba.fastjson.JSON;
import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.File;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.junit.Test;

public class ClientTest {
    @Test
    public void test() throws Exception {
        ClientConfigurationBean configuration = new ClientConfigurationBean();
        configuration.setSecret("OASAD834jA97AAQE761==");

        Tracker tracker = new Tracker();
        tracker.setHost("127.0.0.1");
        tracker.setPort(1022);
        tracker.setMaxConnections(5);
        configuration.addTracker(tracker);

        GoDFSClient client = new GoDFSClient(configuration);
        client.start();
        Thread.sleep(1000);

        final GodfsApiClient apiClient = client.getGodfsApiClient();
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for (int k = 0; k < 100; k++) {
                        File file = null;
                        try {
                            file = apiClient.query("G01/110/M/c595755240f35f167887a6fee4d527ca");
                            System.out.println(JSON.toJSONString(file));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();
        }
        Thread.sleep(5000);
    }
}

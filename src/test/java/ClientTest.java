import com.alibaba.fastjson.JSON;
import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.File;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    private GoDFSClient client;

    @Before
    public void prepare() {
        ClientConfigurationBean configuration = new ClientConfigurationBean();
        configuration.setSecret("OASAD834jA97AAQE761==");

        Tracker tracker = new Tracker();
        tracker.setHost("127.0.0.1");
        tracker.setPort(1022);
        tracker.setMaxConnections(5);
        configuration.addTracker(tracker);

        client = new GoDFSClient(configuration);
        client.start();
    }


    @Test
    public void testQueryFile() throws Exception {
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


    @Test
    public void testUploadFile() {
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        apiClient.upload()
    }


}

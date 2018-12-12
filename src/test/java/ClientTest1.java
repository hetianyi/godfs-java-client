import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.common.UploadProgressMonitor;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ClientTest1 {

    private GoDFSClient client;

    @Before
    public void prepare() {
        ClientConfigurationBean configuration = new ClientConfigurationBean();
        configuration.setSecret("OASAD834jA97AAQE761==");

        Tracker tracker = new Tracker();
        tracker.setHost("192.168.1.141");
        tracker.setPort(1022);
        tracker.setMaxConnections(5);
        configuration.addTracker(tracker);

        Tracker tracker1 = new Tracker();
        tracker1.setHost("192.168.1.141");
        tracker1.setPort(1023);
        tracker1.setMaxConnections(5);
        configuration.addTracker(tracker1);

        client = new GoDFSClient(configuration);
        client.start();
    }

    @Test
    public void testUploadFile() throws Exception {
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        Thread.sleep(3000);
        for (int i = 24; i < 30; i++) {
            File file = new File("E:\\TEMP\\222\\" + i);
            InputStream ips = new FileInputStream(file);
            String path = apiClient.upload(ips, file.length(), null, new UploadProgressMonitor());
            System.out.println(path);
        }

    }



}

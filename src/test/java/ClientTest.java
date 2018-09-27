import com.alibaba.fastjson.JSON;
import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.FileEntity;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.common.UploadProgressMonitor;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
                        FileEntity file = null;
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
    public void testUploadFile() throws Exception {
        Thread.sleep(5000);
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        File file = new File("E:/WorkSpace2018/godfs/dev_tool/mingw-w64-install.exe");
        InputStream ips = new FileInputStream(file);
        String path = apiClient.upload(ips, file.length(), null, new UploadProgressMonitor());
        System.out.println(path);
    }


}

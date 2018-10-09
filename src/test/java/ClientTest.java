import com.alibaba.fastjson.JSON;
import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.bean.FileEntity;
import com.foxless.godfs.bean.Tracker;
import com.foxless.godfs.common.IReader;
import com.foxless.godfs.common.UploadProgressMonitor;
import com.foxless.godfs.config.ClientConfigurationBean;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

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


    //@Test
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


    //@Test
    public void testUploadFile() throws Exception {
        Thread.sleep(5000);
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        File file = new File("D:/123.txt");
        InputStream ips = new FileInputStream(file);
        String path = apiClient.upload(ips, file.length(), null, new UploadProgressMonitor());
        System.out.println(path);
    }
    //@Test
    public void testUploadLocalFile() throws Exception {
        Thread.sleep(3000);
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        File file = new File("E:/CBIMXiongAnManageTools.rar");
        String path = apiClient.upload(file, null, new UploadProgressMonitor());
        System.out.println(path);
    }
    //@Test
    public void testDownload() throws Exception {
        Thread.sleep(3000);
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        OutputStream ops = new FileOutputStream(new File("C:\\Users\\Administrator.WIN-01607081005\\Downloads\\test1111"));
        apiClient.download("G01/110/S/1494b1b549cd9985d9d5c7f1bd2dcdc3", 0, -1, new IReader() {
            @Override
            public void read(byte[] buffer, int start, int len) {
                try {
                    ops.write(buffer, 0, len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void finish() {
                try {
                    ops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //@Test
    public void testMembers() throws Exception {
        Thread.sleep(5000000);
    }



}

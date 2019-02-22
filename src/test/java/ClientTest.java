import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.ClientConfigurationBean;
import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.common.FileVO;
import com.foxless.godfs.common.Tracker;
import com.foxless.godfs.common.UploadProgressMonitor;
import com.foxless.godfs.util.Utils;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class ClientTest {

    private GoDFSClient client;

    @Before
    public void prepare() throws JsonProcessingException {
        ClientConfigurationBean configuration = new ClientConfigurationBean();
        Tracker tracker = new Tracker();
        tracker.setHost("127.0.0.1");
        tracker.setPort(1022);
        tracker.setSecret("OASAD834jA97AAQE761==");
        configuration.addTracker(tracker);
        configuration.setMaxConnections(10);

        client = new GoDFSClient(configuration);
        client.start();
    }


    @Test
    public void testQueryFile() throws Exception {
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileVO file;
                try {
                    file = apiClient.query("G01/vdxj3r3u4lxucjrshywysdvybbxohg/S/cf6a52053ff904bca9d96fd4e7740d7d");
                    ObjectMapper mapper = Utils.getObjectMapper();
                    System.out.println(mapper.writeValueAsString(file));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
        Thread.sleep(1115000);
    }


    @Test
    public void testUploadFile() throws Exception {
        Thread.sleep(2000);
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
//        String path = apiClient.upload(file, null, new UploadProgressMonitor());
//        System.out.println(path);
    }
    //@Test
    public void testDownload() throws Exception {
        Thread.sleep(3000);
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        OutputStream ops = new FileOutputStream(new File("C:\\Users\\Administrator.WIN-01607081005\\Downloads\\test1111"));
//        apiClient.download("G01/110/S/1494b1b549cd9985d9d5c7f1bd2dcdc3", 0, -1, new IDownloadReader() {
//            @Override
//            public void read(byte[] buffer, int start, int len) {
//                try {
//                    ops.write(buffer, 0, len);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void finish() {
//                try {
//                    ops.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    //@Test
    public void testMembers() throws Exception {
        Thread.sleep(5000000);
    }



}

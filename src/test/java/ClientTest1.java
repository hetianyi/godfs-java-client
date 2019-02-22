import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxless.godfs.ClientConfigurationBean;
import com.foxless.godfs.GoDFSClient;
import com.foxless.godfs.api.GodfsApiClient;
import com.foxless.godfs.common.Tracker;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ClientTest1 {

    private GoDFSClient client;

    @Before
    public void prepare() throws JsonProcessingException {
        ClientConfigurationBean configuration = new ClientConfigurationBean();
        Tracker tracker = new Tracker();
        tracker.setHost("192.168.1.164");
        tracker.setPort(1022);
        tracker.setSecret("OASAD834jA97AAQE761==");
        configuration.addTracker(tracker);

        Tracker tracker1 = new Tracker();
        tracker1.setHost("192.168.0.102");
        tracker1.setPort(1023);
        tracker1.setSecret("OASAD834jA97AAQE761==");
        configuration.addTracker(tracker1);
        configuration.setMaxConnections(10);

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
//            String path = apiClient.upload(ips, file.length(), null, new UploadProgressMonitor());
//            System.out.println(path);
        }

    }

    @Test
    public void testUploadFile1() throws Exception {
        final GodfsApiClient apiClient = client.getGodfsApiClient();
        Thread.sleep(3000);
        File file = new File("D:\\图片\\gif\\20120517084154893.gif");
        InputStream ips = new FileInputStream(file);
//        String path = apiClient.upload(ips, file.length(), null, new UploadProgressMonitor());
//        System.out.println(path);

    }

    @Test
    public void testUploadFile2() throws Exception {
        User user = new User();
        user.setAge(12);
        user.setName("李四");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(user));
    }
    @Test
    public void testUploadFile3() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String str = "{\"status\":0,\"uuid\":\"lgomzycnwhukwjesufqhncbgxax4jq\",\"isnew\":true}";
//        OperationValidationResponse ret =mapper.readValue(str, OperationValidationResponse.class);
//        System.out.println(ret);
    }


    private class User {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}

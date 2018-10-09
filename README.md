# godfs-java-client
java client for godfs!

[![Build Status](https://travis-ci.org/hetianyi/godfs-java-client.svg?branch=master)](https://travis-ci.org/hetianyi/godfs-java-client)


maven dependency:
```javascript
<dependency>
  <groupId>com.github.hetianyi</groupId>
  <artifactId>godfs-java-client</artifactId>
  <version>1.0.0</version>
</dependency>
```



Simple usage:
```javascript
# Init client
ClientConfigurationBean configuration = new ClientConfigurationBean();
configuration.setSecret("OASAD834jA97AAQE761==");

Tracker tracker = new Tracker();
tracker.setHost("127.0.0.1");
tracker.setPort(1022);
tracker.setMaxConnections(5);
configuration.addTracker(tracker);

client = new GoDFSClient(configuration);
client.start();

# do something
final GodfsApiClient apiClient = client.getGodfsApiClient();
File file = new File("/tmp/foo/bar");
String path = apiClient.upload(file, null, new UploadProgressMonitor());
System.out.println(path);
```


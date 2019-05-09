package wildengineer.cassandra.data.copy;

import com.datastax.driver.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.annotation.Configurations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by wildengineer on 6/12/16.
 */
@Configuration
public class CassandraConfiguration {

    @Autowired
    private SourceCassandraProperties sourceCassandraProperties;

    @Autowired
    private DestinationCassandraProperties destinationCassandraProperties;

    private Cluster cluster;
    private String cassandraHost = "127.0.0.1";
    private int cassandraPort = 10350;
    private String cassandraUsername = "localhost";
    private String cassandraPassword = "defaultpassword";
    private File sslKeyStoreFile = null;
    private String sslKeyStorePassword = "changeit";
    private String keyspace;

    @Bean
    public Session sourceSession() {
        return buildSession(sourceCassandraProperties);
    }

    @Bean
    public Session destinationSession() throws Exception {
        return buildDestinationSession(destinationCassandraProperties);
    }

    private Session buildSession(CassandraProperties cassandraProperties) {
        //Turning off jmx reporting, since we don't need it and we don't want to bring in any unnecessary dependencies
        //See https://docs.datastax.com/en/developer/java-driver/3.5/manual/metrics/#metrics-4-compatibility
        final SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(500000000).setReadTimeoutMillis(1200000000);

        socketOptions.setKeepAlive(true);

        //socketOptions.setReceiveBufferSize()

        Cluster cluster = Cluster.builder()
                .addContactPoints(cassandraProperties.getContactPoints().split(","))
                .withCredentials(cassandraProperties.getUsername(), cassandraProperties.getPassword())
                .withPort(cassandraProperties.getPort())
                .withSocketOptions(socketOptions)
                .withoutJMXReporting()
                .build();
        cluster.getConfiguration().getPoolingOptions().setMaxConnectionsPerHost(HostDistance.LOCAL, 64);
        cluster.getConfiguration().getPoolingOptions().setMaxConnectionsPerHost(HostDistance.REMOTE, 16);
        return cluster.connect(cassandraProperties.getKeyspace());
    }

    private Session buildDestinationSession(CassandraProperties cassandraProperties) throws Exception {
        loadCassandraConnectionDetails(cassandraProperties);
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        try (final InputStream is = new FileInputStream(sslKeyStoreFile)) {
            keyStore.load(is, sslKeyStorePassword.toCharArray());

        }
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
                .getDefaultAlgorithm());
        kmf.init(keyStore, sslKeyStorePassword.toCharArray());
        final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                .getDefaultAlgorithm());
        tmf.init(keyStore);

        final SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(50000000).setReadTimeoutMillis(120000000);

        // Creates a socket factory for HttpsURLConnection using JKS contents.
        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new java.security.SecureRandom());

        JdkSSLOptions sslOptions = RemoteEndpointAwareJdkSSLOptions.builder()
                .withSSLContext(sc)
                .build();
        cluster = Cluster.builder()
                .addContactPoint(cassandraHost)
                .withPort(cassandraPort)
                .withCredentials(cassandraUsername, cassandraPassword)
                .withSocketOptions(socketOptions)
                .withSSL(sslOptions)
                .withoutJMXReporting()
                .build();
        cluster.getConfiguration().getPoolingOptions().setMaxConnectionsPerHost(HostDistance.LOCAL, 64);
        cluster.getConfiguration().getPoolingOptions().setMaxConnectionsPerHost(HostDistance.REMOTE, 16);
        System.out.println("get keyspace: " + cassandraProperties.getKeyspace());
        return cluster.connect(cassandraProperties.getKeyspace());
    }

    private void loadCassandraConnectionDetails(CassandraProperties cassandraProperties) throws Exception {

        cassandraHost = cassandraProperties.getContactPoints();
        cassandraPort = cassandraProperties.getPort();
        cassandraUsername = cassandraProperties.getUsername();
        cassandraPassword = cassandraProperties.getPassword();
        String ssl_keystore_file_path = null;
        String ssl_keystore_password = null;

        // If ssl_keystore_file_path, build the path using JAVA_HOME directory.
        if (ssl_keystore_file_path == null || ssl_keystore_file_path.isEmpty()) {
            //String javaHomeDirectory = System.getenv("JAVA_HOME");
            //String javaHomeDirectory = "C:\\Program Files\\java\\jdk1.8.0_25";
            String javaHomeDirectory = "/usr/lib/jvm/java-8-openjdk-amd64";
            if (javaHomeDirectory == null || javaHomeDirectory.isEmpty()) {
                throw new Exception("JAVA_HOME not set");
            }
            ssl_keystore_file_path = new StringBuilder(javaHomeDirectory).append("/jre/lib/security/cacerts").toString();
            System.out.println("ssl_keystore_file_path: " + ssl_keystore_file_path);
        }

        sslKeyStorePassword = (ssl_keystore_password != null && !ssl_keystore_password.isEmpty()) ?
                ssl_keystore_password : sslKeyStorePassword;

        sslKeyStoreFile = new File(ssl_keystore_file_path);

        if (!sslKeyStoreFile.exists() || !sslKeyStoreFile.canRead()) {
            throw new Exception(String.format("Unable to access the SSL Key Store file from %s", ssl_keystore_file_path));
        }
    }
}

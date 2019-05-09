package wildengineer.cassandra.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;

@SpringBootApplication(exclude = {CassandraDataAutoConfiguration.class})
public class CassandraDataCopyToolApplication {
	public static void main(String[] args) {
		SpringApplication.run(CassandraDataCopyToolApplication.class, args).close();
		System.exit(0);
	}
}

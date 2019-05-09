package wildengineer.cassandra.data.copy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by wildengineer on 3/28/16.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "copy")
public class CopyProperties extends TuningParams {

	//TODO: Add validation
	
	private String tables;
	private String ignoreColumns;
}


package wildengineer.cassandra.data.copy;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by wildengineer on 5/29/16.
 */
@Getter
@Setter
public class TuningParams {

	private static final int DEFAULT_BATCH_SIZE = 20000; //SIZE OF BATCH TO COPY AT ONCE
	private static final int DEFAULT_QUERY_PAGE_SIZE = 1000; //FETCH QUERY RESULTS IN PAGE SIZE 1000
	private static final int DEFAULT_BATCHES_PER_SECOND = 1; //ONE SECOND

	private int batchSize = DEFAULT_BATCH_SIZE;
	private int queryPageSize = DEFAULT_QUERY_PAGE_SIZE;
	private int batchesPerSecond = DEFAULT_BATCHES_PER_SECOND;

	private int nodeid;



}

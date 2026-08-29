package sd2122.aula10.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface RecordProcessor {
	void onReceive(ConsumerRecord<String, String> r);
}

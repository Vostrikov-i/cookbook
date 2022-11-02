package other.test.examples;

import com.cookbook.processing.annotation.Decode;
import com.cookbook.processing.interfaces.Consumer;
import com.cookbook.processing.interfaces.Decoder;

import java.nio.charset.StandardCharsets;

@Decode(name = "csv_decoder", dataSource = DataSourceCsv.class)
public class DecoderCsv1 implements Decoder<OutputResultCsv> {

    public DecoderCsv1(){}

    private OutputResultCsv data;
    private Consumer<OutputResultCsv> consumers;


    @Override
    public void setData(byte[] data) {
        String s = new String(data, StandardCharsets.UTF_8);
        String[] splitted = s.split(",");
        if (splitted.length > 1) {
            this.data = new OutputResultCsv(splitted[0], splitted[1]);
            this.consumers.notify(this.data);
        }

    }
    @Override
    public void setConsumer(Consumer<OutputResultCsv> consumer) {
        this.consumers = consumer;
    }

}
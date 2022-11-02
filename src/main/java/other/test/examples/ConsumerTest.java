package other.test.examples;

import com.cookbook.processing.annotation.Consume;
import com.cookbook.processing.interfaces.Consumer;

@Consume(name="consumer csv", decoder = {DecoderCsv1.class, DecoderCsv2.class})
public class ConsumerTest implements Consumer<OutputResultCsv> {


    @Override
    public void notify(OutputResultCsv data) {
        System.out.println("Data consumed " + data.toString());
    }
}

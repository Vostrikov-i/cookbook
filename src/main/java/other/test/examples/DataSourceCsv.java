package other.test.examples;

import com.cookbook.processing.annotation.Data_Source;
import com.cookbook.processing.interfaces.DataSource;
import com.cookbook.processing.interfaces.Producer;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;

@Data_Source(name= "csv_data_source")
public class DataSourceCsv implements DataSource {


    private final String path;
    private Producer producer;

    public DataSourceCsv(String path) {
        this.path = path;
    }

    @Override
    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    @Override
    public void getData(){
        File file = new File(this.path);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("read from file " + Arrays.toString(line.getBytes(StandardCharsets.UTF_8)));
                producer.notify(line.getBytes(StandardCharsets.UTF_8));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

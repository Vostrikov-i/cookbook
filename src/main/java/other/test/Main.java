package other.test;

import com.cookbook.math.CalculationResult;
import com.cookbook.math.FunctionWrapper;
import com.cookbook.processing.DataLinker;
import com.cookbook.processing.interfaces.DataSource;
import com.cookbook.processing.interfaces.Producer;
import other.test.examples.DataSourceCsv;

public class Main {

    public static void main(String[] args){
        // Math package
        FunctionWrapper function = FunctionWrapper.newInstance(x -> {return (5.0*Math.sin(2.0*x) + x*x);}, -4, 3);
        CalculationResult res = function.getMinimum(1e-10);
        System.out.println(res.getResult());

        // data processing package
        //Create Linker for Package where store your DataSource, Decoder and Consumer
        DataLinker linker = new DataLinker(other.test.Main.class, com.cookbook.processing.producers.SimpleProducer.class);
        DataSource dataSource = new DataSourceCsv("src\\main\\resources\\test.csv");
        Producer producer = linker.getProducesForDataSourceByName("csv_data_source");
        if(producer!=null) {
            dataSource.setProducer(producer);
            dataSource.getData();
        }



    }



}
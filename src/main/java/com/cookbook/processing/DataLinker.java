package com.cookbook.processing;


import com.cookbook.processing.annotation.Consume;
import com.cookbook.processing.annotation.Data_Source;
import com.cookbook.processing.annotation.Decode;
import com.cookbook.processing.interfaces.Consumer;
import com.cookbook.processing.interfaces.Decoder;
import com.cookbook.processing.interfaces.Producer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Class for create and manage data transfer from data source to consumers
 * Data flow:
 *
 * DataSource (1..1)-> Producer (1..n)-> Decoder (m..n)-> Consumer
 *
 */

public final class DataLinker {


    private final String package_src;
    private List<Class> annotatedClassList;
    private List<Class> dataSourceList;
    private List<Class> decoderList;
    private List<Class> consumerList;
    private final Map<String, Producer> dataSourceContext = new HashMap<>();
    private final Class producerClass;


    /**
     * Public constructor
     * @param appClass - main class for package where store DataSource, Decoders and Consumers
     * @param producerClass - producer class
     */
    public DataLinker(Class<?> appClass, Class producerClass) {
        this.package_src = appClass.getPackage().getName();
        this.producerClass = producerClass;
        initDataSourceContext();

    }

    /**
     * Return producer for DataSource
     * @param dataSourceName - data source name. It set in annotation Data_Source.name()
     *
     */

    public Producer getProducesForDataSourceByName(String dataSourceName) {
        return dataSourceContext.get(dataSourceName);
    }

    /**
     * Create lists for data_sources, decoders, consumers
     * Call Initialize dataSourceContext map
     *
     */
    private void initDataSourceContext() {
        //get only annotated classes
        this.annotatedClassList = ClassManager.findAllClasses(this.package_src).stream().filter(aClass ->
                (aClass.getAnnotation(Data_Source.class) != null || aClass.getAnnotation(Decode.class) != null
                        || aClass.getAnnotation(Consume.class) != null)
        ).collect(Collectors.toList());
        //init inputData, Decoder, dataSource, OutputData lists
        this.dataSourceList = this.annotatedClassList.stream().filter(aClass -> (aClass.getAnnotation(Data_Source.class) != null))
                .collect(Collectors.toList());
        this.decoderList = this.annotatedClassList.stream().filter(aClass -> (aClass.getAnnotation(Decode.class) != null))
                .collect(Collectors.toList());
        this.consumerList = this.annotatedClassList.stream().filter(aClass -> (aClass.getAnnotation(Consume.class) != null))
                .collect(Collectors.toList());

        createLinkedContext();

    }
    /**
     * Initialize dataSourceContext map, key-> DataSource name (Set in annotation Data_Source.name())
     * value -> Producer for this DataSource
     */
    private void createLinkedContext() {
        for (Class dataSource : this.dataSourceList) {

            Data_Source ann = (Data_Source) dataSource.getAnnotation(Data_Source.class);
            Producer producer = producerFactory(ann, dataSource);
            if(producer!=null) {
                dataSourceContext.put(ann.name(), producer);
            }
        }
    }

    /**
     * Producer Factory
     * Create producer for this data source and link it with decoders
     * Links based on annotations
     */
    private Producer producerFactory(Data_Source ann, Class dataSource) {
        //check linked decoders for this datasource
        Producer producer = getProducerFromClass(this.producerClass);
        if(producer!=null) {
            for (Class decoder : this.decoderList) {
                Decode dec_ann = (Decode) decoder.getAnnotation(Decode.class);
                if (dec_ann.dataSource() == dataSource) {
                    Decoder decoder1 = decoderFactory(decoder);
                    if (decoder1 != null) {
                        producer.addDecoder(decoder1);
                    }
                }
            }
        }
        return producer;
    }

    /**
     * create producer Instance from Class
     */
    private Producer getProducerFromClass(Class producerClass) {
        try {
            if (Arrays.stream(producerClass.getInterfaces()).filter(x->x==Producer.class).count() >0) {
                Constructor<?> constructor = producerClass.getConstructor();
                Producer producer = (Producer) constructor.newInstance();

                return producer;
            }
        } catch (Exception ex) {
            return null;
        }
        return null;

    }
    /**
     * Decoder Factory
     * Create decoders for this data source and link with consumers
     * Links based on annotations
     */
    private Decoder decoderFactory(Class decoder){
        try {
            if (Arrays.stream(decoder.getInterfaces()).filter(x->x==Decoder.class).count() >0) {
                Constructor<?> constructor = decoder.getConstructor();
                Decoder decoder1 = (Decoder) constructor.newInstance();
                for(Class consumer : this.consumerList) {
                   Consumer consumer1 = consumerFactory(consumer);
                   if(consumer1!=null) {
                       decoder1.setConsumer(consumer1);
                   }
                }
                return decoder1;
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            return null;
        }
        return null;
    }
    /**
     * Consumers Factory
     * Create consumer Instance from Class
     */
    private Consumer consumerFactory(Class consumer){
        try {

            if (Arrays.stream(consumer.getInterfaces()).filter(x->x==Consumer.class).count() >0) {
                Constructor<?> constructor = consumer.getConstructor();
                Consumer consumer1 = (Consumer) constructor.newInstance();
                return consumer1;
            }

        } catch (Exception ex) {
            return null;
        }
        return null;
    }
}
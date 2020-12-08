package com.maverick.springbatchexample.configuration;

import com.maverick.springbatchexample.model.Person;
import com.maverick.springbatchexample.processor.PersonItemProcessor;
import com.maverick.springbatchexample.service.JobCompletionNotificationListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Value("${files.source-file:sample-data.csv}")
    private String sourceFile;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource(sourceFile))
                .delimited()
                .names(new String[]{"name", "age", "gender"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>(){{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    @StepScope
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO M_HUMAN (name, age, gender) VALUES (:name, :age, :gender)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importPersonJob(JobCompletionNotificationListener listener,
                               @Qualifier("personImportStep") Step personImportStep) {
        return jobBuilderFactory.get("importPersonJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(personImportStep)
                .build();
    }

    @Bean
    public Step personImportStep(FlatFileItemReader<Person> reader,
                                 PersonItemProcessor processor,
                                 JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("personImportStep")
                .<Person, Person> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

}

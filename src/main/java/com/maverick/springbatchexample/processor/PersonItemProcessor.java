package com.maverick.springbatchexample.processor;

import com.maverick.springbatchexample.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger LOG = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(Person person) throws Exception {
        LOG.info("### Process: " + person.getName());
        return person;
    }

}

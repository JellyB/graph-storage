package com.wdcloud.data.neo4j.service;

import com.wdcloud.data.neo4j.domain.Person;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * @author pdtyreus
 * @author Mark Angrish
 */
@Repository
public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {

}

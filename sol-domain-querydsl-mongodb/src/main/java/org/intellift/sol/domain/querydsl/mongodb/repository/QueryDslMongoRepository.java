package org.intellift.sol.domain.querydsl.mongodb.repository;

import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.mongodb.repository.MongoRepository;
import org.intellift.sol.domain.querydsl.repository.QueryDslRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface QueryDslMongoRepository<T extends Identifiable<ID>, ID extends Serializable> extends QueryDslRepository<T, ID>, MongoRepository<T, ID> {
}

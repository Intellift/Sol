package org.intellift.sol.domain.mongodb.repository;

import org.intellift.sol.domain.Identifiable;
import org.intellift.sol.domain.repository.Repository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface MongoRepository<E extends Identifiable<ID>, ID extends Serializable> extends Repository<E, ID>, org.springframework.data.mongodb.repository.MongoRepository<E, ID> {
}

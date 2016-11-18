package org.intellift.sol.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface PageMapper<E, D> extends Mapper<E, D> {

    default Page<D> mapTo(Page<E> page) {
        final List<D> objects = mapTo(page.getContent());

        return new PageImpl<>(
                new ArrayList<>(objects),
                new PageRequest(page.getNumber(), page.getSize()),
                page.getTotalElements()
        );
    }

    default Page<E> mapFrom(Page<D> page) {
        final List<E> objects = mapFrom(page.getContent());

        return new PageImpl<>(
                new ArrayList<>(objects),
                new PageRequest(page.getNumber(), page.getSize()),
                page.getTotalElements()
        );
    }
}

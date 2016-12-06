package org.intellift.sol.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public interface PageMapper<E, D> extends Mapper<E, D> {

    default Page<D> mapTo(final Page<E> page) {
        Objects.requireNonNull(page, "page is null");

        final List<D> objects = mapTo(page.getContent());

        return new PageImpl<>(
                new ArrayList<>(objects),
                new PageRequest(page.getNumber(), page.getSize()),
                page.getTotalElements()
        );
    }

    default Page<E> mapFrom(final Page<D> page) {
        Objects.requireNonNull(page, "page is null");

        final List<E> objects = mapFrom(page.getContent());

        return new PageImpl<>(
                new ArrayList<>(objects),
                new PageRequest(page.getNumber(), page.getSize()),
                page.getTotalElements()
        );
    }
}

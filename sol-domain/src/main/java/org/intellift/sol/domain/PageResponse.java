package org.intellift.sol.domain;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public class PageResponse<D> extends PageImpl<D> {

    public PageResponse(List<D> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public PageResponse(List<D> content) {
        super(content);
    }

    public PageResponse() {
        super(new ArrayList<>());
    }
}

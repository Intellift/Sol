package org.intellift.sol.sdk.client.internal;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Achilleas Naoumidis, Chrisostomos Bakouras
 */
public class PageResponse<D> extends PageImpl<D> {

    public PageResponse(final List<D> content, final Pageable pageable, final long total) {
        super(content, pageable, total);
    }

    public PageResponse(final List<D> content) {
        super(content);
    }

    public PageResponse() {
        super(new ArrayList<>());
    }
}

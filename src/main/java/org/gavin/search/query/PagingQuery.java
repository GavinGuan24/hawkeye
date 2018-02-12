package org.gavin.search.query;

import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * ---------------------------------------------------
 * File:    PagingQuery
 * Package: org.gavin.search.query
 * Project: hawkeye
 * ---------------------------------------------------
 * Created by gavinguan on 2018/1/23 13:45.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public class PagingQuery {

    //命中上限: topSearch模式下, 命中结果上限 9kw
    public static final int HITTING_MAX = 90000000;

    private Query coreQuery;
    private int pageIndex;
    private int pageSize;
    private boolean highlight;

    public PagingQuery(Query coreQuery, int pageIndex, int pageSize) throws IOException {
        this(coreQuery, pageIndex, pageSize, false);
    }

    public PagingQuery(Query coreQuery, int pageIndex, int pageSize, boolean highlight) throws IOException {
        if (coreQuery == null) throw new IOException("Query Mustn't be Null");
        this.coreQuery = coreQuery;

        if (pageSize <= 0 || pageSize > 101) pageSize = 10;
        this.pageSize = pageSize;

        int maxPageIndex = HITTING_MAX / pageSize + ((HITTING_MAX % pageSize > 0)?1:0);

        if (pageIndex <= 0 || pageIndex > maxPageIndex) pageIndex = 1;
        this.pageIndex = pageIndex;

        this.highlight = highlight;
    }

    public int getPageOffset() {
        return (this.pageIndex - 1) * pageSize;
    }

    public int getHittingMax() {
        return HITTING_MAX;
    }

    public Query getCoreQuery() {
        return coreQuery;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean getHighlight() {
        return highlight;
    }
}

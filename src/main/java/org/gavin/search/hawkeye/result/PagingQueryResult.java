package org.gavin.search.hawkeye.result;

import java.util.List;

/**
 * ---------------------------------------------------
 * File:    PagingQueryResult
 * Package: org.gavin.search.hawkeye.result
 * Project: hawkeye
 * ---------------------------------------------------
 * Created by gavinguan on 2018/1/23 13:04.
 * Copyright © 2018 gavinguan. All rights reserved.
 */
public class PagingQueryResult<T> {

    private int totalHits;
    private List<T> pagingDatas;
    private int pageIndex;
    private int pageSize;

    public PagingQueryResult(int totalHits, List<T> pagingDatas, int pageIndex, int pageSize) {
        this.totalHits = totalHits;
        this.pagingDatas = pagingDatas;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public List<T> getPagingDatas() {
        return pagingDatas;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }
}

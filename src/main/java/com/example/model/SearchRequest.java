package com.example.model;

import java.util.List;

public class SearchRequest {

    private List<SearchFilter> filters;
    private List<SearchSort> sorts;
    private PageRequest page;

    public List<SearchFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilter> filters) {
        this.filters = filters;
    }

    public List<SearchSort> getSorts() {
        return sorts;
    }

    public void setSorts(List<SearchSort> sorts) {
        this.sorts = sorts;
    }

    public PageRequest getPage() {
        return page;
    }

    public void setPage(PageRequest page) {
        this.page = page;
    }
}

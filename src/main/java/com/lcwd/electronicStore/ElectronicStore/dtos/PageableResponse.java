package com.lcwd.electronicStore.ElectronicStore.dtos;

/*
Purpose:
Wraps paginated API results with page metadata.
*/
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageableResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElement;
    private int totalPages;
    private boolean lastPage;




}

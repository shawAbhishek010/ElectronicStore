package com.lcwd.electronicStore.ElectronicStore.helper;
import com.lcwd.electronicStore.ElectronicStore.dtos.PageableResponse;
import com.lcwd.electronicStore.ElectronicStore.dtos.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


//THIS CLASS CONTAIN METHODS THAT CAN BE REUSED NAD USED TO SEND CLEAN API CALLS FOR PAGINATION.............
@Component
public class PageableHelper {

    public <U, V> PageableResponse<V> getPageableResponse(Page<U> page,Class<V> type) {

        List<V> dtoList = page.getContent() // get content from Page
                .stream()
                .map(Object ->  new ModelMapper().map(Object, type))
                .collect(Collectors.toList());

        PageableResponse<V> response = new PageableResponse<>();
        response.setContent(dtoList);
        response.setLastPage(page.isLast());
        response.setPageNumber(page.getNumber()+1);
        response.setPageSize(page.getSize());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElement(page.getTotalElements());

        return response;
    }
}


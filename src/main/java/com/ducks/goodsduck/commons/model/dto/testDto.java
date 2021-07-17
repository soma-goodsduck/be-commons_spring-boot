package com.ducks.goodsduck.commons.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class testDto {

    private String name;
    private List<ImageDto> a = new ArrayList<>();
}

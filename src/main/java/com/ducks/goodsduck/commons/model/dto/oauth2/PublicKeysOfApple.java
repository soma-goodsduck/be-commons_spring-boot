package com.ducks.goodsduck.commons.model.dto.oauth2;

import lombok.Data;

import java.util.List;

@Data
public class PublicKeysOfApple {
    private List<PublicKeyOfApple> keys;
}

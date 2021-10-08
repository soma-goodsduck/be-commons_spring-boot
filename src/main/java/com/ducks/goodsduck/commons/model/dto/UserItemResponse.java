package com.ducks.goodsduck.commons.model.dto;

import com.ducks.goodsduck.commons.model.entity.UserItem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserItemResponse {

    private String senderNickName;
    private String senderImageUri;
    private String itemName;

    public UserItemResponse(UserItem userItem) {
        this.senderNickName = userItem.getUser().getNickName();
        this.senderImageUri = userItem.getUser().getImageUrl();
        this.itemName = userItem.getItem().getName();
    }

    public UserItemResponse(String senderNickName, String senderImageUri, String itemName) {
        this.senderNickName = senderNickName;
        this.senderImageUri = senderImageUri;
        this.itemName = itemName;
    }
}

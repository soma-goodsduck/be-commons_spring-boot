package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.UserChatDto;
import com.ducks.goodsduck.commons.model.dto.user.UserDto;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserChatService {

    private final ItemRepository itemRepository;
    private final UserChatRepository userChatRepository;
    private final UserChatRepositoryCustom userChatRepositoryCustom;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final PriceProposeRepository priceProposeRepository;

    public Boolean createWithImmediateTrade(String chatId, Long userId, Long itemId) {

        Chat chat = new Chat(chatId);
        chatRepository.save(chat);

        Item item = itemRepository.findById(itemId).get();

        User user= userRepository.findById(userId).get();
        User itemOwner = item.getUser();

        UserChat userChat = new UserChat(user, chat, item);
        UserChat itemOwnerChat = new UserChat(itemOwner, chat, item);

        userChatRepository.save(userChat);
        userChatRepository.save(itemOwnerChat);

        return true;
    }

    public Boolean createWithPricePropose(String chatId, Long itemOwnerId, Long priceProposeId) {

        Chat chat = new Chat(chatId);
        chatRepository.save(chat);

        PricePropose pricePropose = priceProposeRepository.findById(priceProposeId).get();
        Item item = pricePropose.getItem();

        User proposer = pricePropose.getUser();
        User itemOwner = userRepository.findById(itemOwnerId).get();

        UserChat proposerChat = new UserChat(proposer, chat, item);
        UserChat itemOwnerChat = new UserChat(itemOwner, chat, item);

        userChatRepository.save(proposerChat);
        userChatRepository.save(itemOwnerChat);

        return true;
    }

    public UserChatDto getChatInfo(String chatId, Long userId) throws IllegalAccessException {

        User checkUser = userRepository.findById(userId).get();
        Boolean check = false;

        List<UserChat> userChatList = userChatRepositoryCustom.findAllByChatId(chatId);
        List<User> userList = new ArrayList<>();

        for (UserChat userChat : userChatList) {

            User user = userChat.getUser();
            userList.add(user);

            if(checkUser.equals(user)) {
                check = true;
            }
        }

        if(!check) {
            throw new IllegalAccessException("Do not access this chat room");
        }

        return new UserChatDto(userList, userChatList.get(0).getItem());
    }
}

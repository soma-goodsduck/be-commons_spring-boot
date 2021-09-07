package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.review.TradeCompleteReponse;
import com.ducks.goodsduck.commons.model.dto.chat.ChatRoomDto;
import com.ducks.goodsduck.commons.model.dto.chat.UserChatDto;
import com.ducks.goodsduck.commons.model.dto.chat.UserChatResponse;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.review.ReviewRepository;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private final ItemImageRepository itemImageRepository;
    private final ImageRepositoryCustomImpl imageRepositoryCustomImpl;

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

    public Boolean deleteChat(String chatId) throws Exception {

        try {
            chatRepository.deleteById(chatId);
            List<UserChat> userChatList = userChatRepositoryCustom.findAllByChatId(chatId);
            userChatRepository.deleteInBatch(userChatList);
            return true;
        } catch (Exception e) {
            throw new Exception("Fail to delete Chat");
        }
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

    public List<ChatRoomDto> getChatRooms(Long userId) {

        List<Tuple> chatAndItemByUserIdTuple = userChatRepositoryCustom.findChatAndItemByUserId(userId);

        List<ChatRoomDto> chatAndItemList = chatAndItemByUserIdTuple.stream().
                map(tuple -> {
                    Chat chat = tuple.get(0, Chat.class);
                Item item = tuple.get(1, Item.class);

                    return new ChatRoomDto(chat, item);
                })
                .collect(Collectors.toList());

        return chatAndItemList;
    }

    public List<ChatRoomDto> getChatRoomsV2(Long userId) {

        List<UserChat> userChats = userChatRepository.findAll();

        return userChats.stream()
                .map(userChat -> {
                    Chat chat = userChat.getChat();
                    Item item = userChat.getItem();

                    User user = userChat.getUser();

                    return new ChatRoomDto(chat, item, user.getNickName());
                })
                .collect(Collectors.toList());

    }

    public String getChatIdByUserIdAndItemOwnerId(Long userId) {
        return "1";
    }

    public List<UserChatResponse> findByItemId(Long itemOwnerId, Long itemId) throws IllegalAccessException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.debug("Input itemId is not valid. itemId: {}", itemId);
                    throw new NoResultException("Item not founded");
                });

        // HINT: 해당 유저가 아이템 게시글 주인이 아닌 경우 접근 제한
        if (!item.getUser().getId().equals(itemOwnerId)) {
            log.debug("This user is not owner of this item. itemId: {}", itemId);
            throw new IllegalAccessException("Cannot access except item owner.");
        }

        return userChatRepositoryCustom.findByItemIdExceptItemOwner(itemOwnerId, itemId)
                .stream()
                .map(tuple -> {
                    UserChat userChat = tuple.get(0, UserChat.class);
                    return new UserChatResponse(userChat); })
                .collect(Collectors.toList());
    }

    public TradeCompleteReponse findByItemIdV2(Long itemOwnerId, Long itemId) throws IllegalAccessException {
        // HINT: 해당 유저가 굿즈에 대한 리뷰를 남긴 적이 있으면 TradeCompleteResponse.exist = false 넣어서 반환
        if (reviewRepository.existsByItemIdAndUserId(itemId, itemOwnerId)) {
            TradeCompleteReponse emptyTradeCompleteReponse = new TradeCompleteReponse();
            emptyTradeCompleteReponse.exist();
            return emptyTradeCompleteReponse;
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.debug("Input itemId is not valid. itemId: {}", itemId);
                    throw new NoResultException("Item not founded");
                });

        // HINT: 해당 유저가 아이템 게시글 주인이 아닌 경우 접근 제한
        if (!item.getUser().getId().equals(itemOwnerId)) {
            log.debug("This user is not owner of this item. itemId: {}", itemId);
            throw new IllegalAccessException("Cannot access except item owner.");
        }

        List<UserChatResponse> userChatResponses = userChatRepositoryCustom.findByItemIdExceptItemOwner(itemOwnerId, itemId)
                .stream()
                .map(tuple -> new UserChatResponse(
                        tuple.get(0, UserChat.class)
                ))
                .collect(Collectors.toList());

        return new TradeCompleteReponse(item, userChatResponses);
    }
}

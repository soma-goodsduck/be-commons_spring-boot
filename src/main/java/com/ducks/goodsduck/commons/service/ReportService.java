package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.exception.common.DuplicatedDataException;
import com.ducks.goodsduck.commons.exception.common.InvalidRequestDataException;
import com.ducks.goodsduck.commons.exception.common.NotFoundDataException;
import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryResponse;
import com.ducks.goodsduck.commons.model.dto.report.*;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.category.Category;
import com.ducks.goodsduck.commons.model.entity.report.*;
import com.ducks.goodsduck.commons.repository.chat.ChatRepository;
import com.ducks.goodsduck.commons.repository.report.ItemReportRepositoryCustom;
import com.ducks.goodsduck.commons.repository.report.ItemReportRepositoryCustomImpl;
import com.ducks.goodsduck.commons.repository.report.ReportRepository;
import com.ducks.goodsduck.commons.repository.category.*;
import com.ducks.goodsduck.commons.repository.comment.CommentRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import com.ducks.goodsduck.commons.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChatRepository chatRepository;
    private final ItemReportRepositoryCustom itemReportRepositoryCustom;

    private final CategoryRepository categoryRepository;
    private final PostReportCategoryRepository postReportCategoryRepository;
    private final ItemReportCategoryRepository itemReportCategoryRepository;
    private final CommentReportCategoryRepository commentReportCategoryRepository;
    private final ChatReportCategoryRepository chatReportCategoryRepository;
    private final UserReportCategoryRepository userReportCategoryRepository;

    private final MessageSource messageSource;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository, ItemRepository itemRepository, PostRepository postRepository, CommentRepository commentRepository, ChatRepository chatRepository, ItemReportRepositoryCustomImpl itemReportRepositoryCustomImpl, CategoryRepository categoryRepository, PostReportCategoryRepository postReportCategoryRepository, ItemReportCategoryRepository itemReportCategoryRepository, CommentReportCategoryRepository commentReportCategoryRepository, ChatReportCategoryRepository chatReportCategoryRepository, UserReportCategoryRepository userReportCategoryRepository, MessageSource messageSource) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.chatRepository = chatRepository;
        this.itemReportRepositoryCustom = itemReportRepositoryCustomImpl;
        this.categoryRepository = categoryRepository;
        this.postReportCategoryRepository = postReportCategoryRepository;
        this.itemReportCategoryRepository = itemReportCategoryRepository;
        this.commentReportCategoryRepository = commentReportCategoryRepository;
        this.chatReportCategoryRepository = chatReportCategoryRepository;
        this.userReportCategoryRepository = userReportCategoryRepository;
        this.messageSource = messageSource;
    }

    public ReportResponse addReport(Long senderId, ReportRequest reportRequest) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"User"}, null)));

        User receiver = userRepository.findByBcryptId(reportRequest.getReceiverBcryptId());
        if (receiver == null) {
            throw new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                    new Object[]{"User"}, null));
        }

        Category reportCategory = categoryRepository.findById(reportRequest.getReportCategoryId())
                .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Category"}, null)));

        String reportType = reportRequest.getType();

        if(reportType.equals("UserReport")) {
            UserReport userReport = new UserReport(reportRequest, reportCategory, sender, receiver);
            return new ReportResponse(reportRepository.save(userReport));
        }
        else if(reportType.equals("ItemReport")) {
            Item item = itemRepository.findById(Long.parseLong(reportRequest.getId()))
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Item"}, null)));
            if (itemReportRepositoryCustom.existsBySenderIdAndItemId(senderId, item.getId())) {
                throw new DuplicatedDataException(messageSource.getMessage(DuplicatedDataException.class.getSimpleName(),
                        new Object[]{"ItemReport"}, null));
            }
            ItemReport itemReport = new ItemReport(reportRequest, reportCategory, sender, receiver, item);
            return new ReportResponse(reportRepository.save(itemReport));
        }
        else if(reportType.equals("ChatReport")) {
            Chat chat = chatRepository.findById(reportRequest.getId())
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Chat"}, null)));
            ChatReport chatReport = new ChatReport(reportRequest, reportCategory, sender, receiver, chat);
            return new ReportResponse(reportRepository.save(chatReport));
        }
        else if(reportType.equals("PostReport")) {
            Post post = postRepository.findById(Long.parseLong(reportRequest.getId()))
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Post"}, null)));
            PostReport postReport = new PostReport(reportRequest, reportCategory, sender, receiver, post);
            return new ReportResponse(reportRepository.save(postReport));
        }
        else if(reportType.equals("CommentReport")) {
            Comment comment = commentRepository.findById(Long.parseLong(reportRequest.getId()))
                    .orElseThrow(() -> new NotFoundDataException(messageSource.getMessage(NotFoundDataException.class.getSimpleName(),
                        new Object[]{"Comment"}, null)));
            CommentReport commentReport = new CommentReport(reportRequest, reportCategory, sender, receiver, comment);
            return new ReportResponse(reportRepository.save(commentReport));
        }
        else {
            throw new InvalidRequestDataException("Invalid report type requested.");
        }
    }

    public ReportCategoryResponse getItemReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getItemReportCategory");
        }

        List<CategoryResponse> reportCategoryList = itemReportCategoryRepository.findAll()
                .stream()
                .map(postReportCategory -> new CategoryResponse(postReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }

    public ReportCategoryResponse getPostReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getPostReportCategory");
        }

        List<CategoryResponse> reportCategoryList = postReportCategoryRepository.findAll()
                .stream()
                .map(postReportCategory -> new CategoryResponse(postReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }

    public ReportCategoryResponse getCommentReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getCommentReportCategory");
        }

        List<CategoryResponse> reportCategoryList = commentReportCategoryRepository.findAll()
                .stream()
                .map(commentReportCategory -> new CategoryResponse(commentReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }

    public ReportCategoryResponse getChatReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getChatReportCategory");
        }

        List<CategoryResponse> reportCategoryList = chatReportCategoryRepository.findAll()
                .stream()
                .map(chatReportCategory -> new CategoryResponse(chatReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }

    public ReportCategoryResponse getUserReportCategory(String bcryptId) {

        User receiver = userRepository.findByBcryptId(bcryptId);
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.getUserReportCategory");
        }

        List<CategoryResponse> reportCategoryList = userReportCategoryRepository.findAll()
                .stream()
                .map(userReportCategory -> new CategoryResponse(userReportCategory))
                .collect(Collectors.toList());

        return new ReportCategoryResponse(receiver.getNickName(), reportCategoryList);
    }


    // TODO : 백오피스 ㄱㄱ
//    public List<ReportResponse> getReportsForUser(Long userId, Long receiverId) throws IllegalAccessException {
//        User loginUser = userRepository.findById(userId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        User receiver = userRepository.findById(receiverId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        if (userId.equals(receiverId)) {
//            throw new IllegalArgumentException("신고하려는 대상이 본인입니다.");
//        }
//
//        if (!loginUser.getRole().equals(ADMIN)) {
//            throw new IllegalAccessException("관리자 권한이 필요합니다.");
//        }
//
//        return reportRepository.findByReceiver(receiver)
//                .stream()
//                .map(report -> new ReportResponse(report))
//                .collect(Collectors.toList());
//    }
//
//    public CategoryReportAddRequest addCategoryReport(Long userId, CategoryReportAddRequest categoryReportRequest) throws IllegalAccessException {
//        // HINT: 관리자가 아니면 예외 처리
//        User loginUser = userRepository.findById(userId).orElseThrow(() -> {
//            throw new NoResultException("User not founded.");
//        });
//
//        if (!loginUser.getRole().equals(ADMIN)) throw new IllegalAccessException("관리자 권한이 필요합니다.");
//
//        return new CategoryReportAddRequest(categoryReportRepository.save(new CategoryReport(categoryReportRequest)));
//    }
}


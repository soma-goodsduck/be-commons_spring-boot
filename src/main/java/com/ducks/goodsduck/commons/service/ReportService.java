package com.ducks.goodsduck.commons.service;

import com.ducks.goodsduck.commons.model.dto.category.CategoryResponse;
import com.ducks.goodsduck.commons.model.dto.category.ReportCategoryResponse;
import com.ducks.goodsduck.commons.model.dto.report.*;
import com.ducks.goodsduck.commons.model.entity.*;
import com.ducks.goodsduck.commons.model.entity.category.Category;
import com.ducks.goodsduck.commons.model.entity.report.*;
import com.ducks.goodsduck.commons.repository.*;
import com.ducks.goodsduck.commons.repository.ReportRepository.ReportRepository;
import com.ducks.goodsduck.commons.repository.category.*;
import com.ducks.goodsduck.commons.repository.comment.CommentRepository;
import com.ducks.goodsduck.commons.repository.item.ItemRepository;
import com.ducks.goodsduck.commons.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChatRepository chatRepository;

    private final CategoryRepository categoryRepository;
    private final PostReportCategoryRepository postReportCategoryRepository;
    private final ItemReportCategoryRepository itemReportCategoryRepository;
    private final CommentReportCategoryRepository commentReportCategoryRepository;
    private final ChatReportCategoryRepository chatReportCategoryRepository;

    public ReportResponse addReport(Long userId, ReportRequest reportRequest) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> { throw new NoResultException("Not Find User in CategoryService.addReport");});

        User receiver = userRepository.findByBcryptId(reportRequest.getReceiverBcryptId());
        if (receiver == null) {
            throw new NoResultException("Not Find User in CategoryService.addReport");
        }

        Category reportCategory = categoryRepository.findById(reportRequest.getReportCategoryId())
                .orElseThrow(() -> { throw new NoResultException("Not Find ReportCategory in CategoryService.addReport");});

        if(reportRepository.existsByReceiverAndSenderId(receiver, sender.getId())) {
            return new ReportResponse();
        }

        if(reportRequest.getType().equals("UserReport")) {
            UserReport userReport = new UserReport(reportRequest, reportCategory, sender, receiver);
            return new ReportResponse(reportRepository.save(userReport));
        }
        else if(reportRequest.getType().equals("ItemReport")) {
            Item item = itemRepository.findById(Long.parseLong(reportRequest.getId())).get();
            ItemReport itemReport = new ItemReport(reportRequest, reportCategory, sender, receiver, item);
            return new ReportResponse(reportRepository.save(itemReport));
        }
        else if(reportRequest.getType().equals("ChatReport")) {
            Chat chat = chatRepository.findById(reportRequest.getId()).get();
            ChatReport chatReport = new ChatReport(reportRequest, reportCategory, sender, receiver, chat);
            return new ReportResponse(reportRepository.save(chatReport));
        }
        else if(reportRequest.getType().equals("PostReport")) {
            Post post = postRepository.findById(Long.parseLong(reportRequest.getId())).get();
            PostReport postReport = new PostReport(reportRequest, reportCategory, sender, receiver, post);
            return new ReportResponse(reportRepository.save(postReport));
        }
        else if(reportRequest.getType().equals("CommentReport")) {
            Comment comment = commentRepository.findById(Long.parseLong(reportRequest.getId())).get();
            CommentReport commentReport = new CommentReport(reportRequest, reportCategory, sender, receiver, comment);
            return new ReportResponse(reportRepository.save(commentReport));
        }
        else {
            return null;
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
                .map(chat -> new CategoryResponse(chat))
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


package com.ducks.goodsduck.commons.model.entity.report;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.entity.Chat;
import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.category.Category;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("CommentReport")
public class ChatReport extends Report {

    private String chatId;

    public ChatReport(ReportRequest reportRequest, Category category, User sender, User receiver, Chat chat) {
        super(reportRequest, category, sender, receiver);
        this.chatId = chat.getId();
    }
}

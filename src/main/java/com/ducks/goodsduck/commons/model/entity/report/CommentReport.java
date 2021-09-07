package com.ducks.goodsduck.commons.model.entity.report;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.entity.Comment;
import com.ducks.goodsduck.commons.model.entity.Item;
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
public class CommentReport extends Report {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    public CommentReport(ReportRequest reportRequest, Category category, User sender, User receiver, Comment comment) {
        super(reportRequest, category, sender, receiver);
        this.comment = comment;
    }
}

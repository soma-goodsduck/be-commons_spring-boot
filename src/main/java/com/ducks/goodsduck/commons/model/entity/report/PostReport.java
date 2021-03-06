package com.ducks.goodsduck.commons.model.entity.report;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.entity.Item;
import com.ducks.goodsduck.commons.model.entity.Post;
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
@DiscriminatorValue("PostReport")
public class PostReport extends Report {

    private Long postId;

    public PostReport(ReportRequest reportRequest, Category category, User sender, User receiver, Post post) {
        super(reportRequest, category, sender, receiver);
        this.postId = post.getId();
    }
}

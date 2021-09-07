package com.ducks.goodsduck.commons.model.entity.report;

import com.ducks.goodsduck.commons.model.dto.report.ReportRequest;
import com.ducks.goodsduck.commons.model.entity.User;
import com.ducks.goodsduck.commons.model.entity.category.Category;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("UserReport")
public class UserReport extends Report {

    private Long userId;

    public UserReport(ReportRequest reportRequest, Category category, User sender, User receiver) {
        super(reportRequest, category, sender, receiver);
        this.userId = receiver.getId();
    }
}
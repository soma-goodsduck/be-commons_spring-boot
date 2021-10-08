package com.ducks.goodsduck.commons.model.entity.category;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("PostReport")
public class PostReportCategory extends Category {
}

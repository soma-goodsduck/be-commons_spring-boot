package com.ducks.goodsduck.commons.repository.report;

import com.ducks.goodsduck.commons.model.entity.report.QItemReport;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class ItemReportRepositoryCustomImpl implements ItemReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QItemReport itemReport = QItemReport.itemReport;

    public ItemReportRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Boolean existsBySenderIdAndItemId(Long senderId, Long itemId) {
        return queryFactory
                .select(itemReport)
                .from(itemReport)
                .where(itemReport.senderId.eq(senderId)
                        .and(itemReport.itemId.eq(itemId)))
                .fetchFirst() != null;
    }
}

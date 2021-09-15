package com.ducks.goodsduck.commons.repository.report;

import org.springframework.stereotype.Repository;

@Repository
public interface ItemReportRepositoryCustom {
    Boolean existsBySenderIdAndItemId(Long senderId, Long itemId);
}

package com.ducks.goodsduck.commons.repository.image;

import com.ducks.goodsduck.commons.model.entity.Image.Image;
import com.ducks.goodsduck.commons.model.entity.Image.QImage;
import com.ducks.goodsduck.commons.model.entity.Image.QItemImage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ImageRepositoryCustomImpl implements ImageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private QImage image = QImage.image;
    private QItemImage itemImage = QItemImage.itemImage;

    public ImageRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Image> findByImageUrls(List<String> imageUrls) {

        BooleanBuilder builder = new BooleanBuilder();

        for (String imageUrl : imageUrls) {
            builder.or(image.url.eq(imageUrl));
        }

        return queryFactory
                .select(image)
                .from(image)
                .where(builder)
                .fetch();
    }

    @Override
    public List<Image> findItemImages() {
        return queryFactory
                .select(image)
                .from(image)
                .leftJoin(itemImage).on(image.id.eq(itemImage.id))
                .fetch();
    }
}

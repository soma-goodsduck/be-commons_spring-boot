package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.PriceProposeRequest;
import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.service.JwtService;
import com.ducks.goodsduck.commons.service.PriceProposeService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PriceProposeController {

    private final PriceProposeService priceProposeService;
    private final JwtService jwtService;

    /** 가격 제안 요청 API */
    @PostMapping("/item/{itemId}/propose")
    public PriceProposeResponse proposePrice(@PathVariable("itemId") Long itemId, @RequestHeader("jwt") String jwt, PriceProposeRequest priceProposeRequest) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.proposePrice(userId, itemId, priceProposeRequest.getPrice());
    }

    /** 요청했던 가격 제안에 대한 취소 요청 API */

    /** 요청했던 가격 제안에 대한 제안 가격 변경 API */

    /** 받은 가격 제안에 대한 상태 변경 요청(수락/거절) API*/

    /** 특정 게시글에 대한 가격 제안 요청 목록 보기 API */

    /** 특정 유저에 대한 가격 제안 요청 목록 보기 API */


}

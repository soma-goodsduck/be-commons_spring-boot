package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeRequest;
import com.ducks.goodsduck.commons.model.dto.pricepropose.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.ducks.goodsduck.commons.service.NotificationService;
import com.ducks.goodsduck.commons.service.PriceProposeService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
@Api(tags = "가격 제안 APIs")
public class PriceProposeController {

    private final PriceProposeService priceProposeService;
    private final NotificationService notificationService;

    @PostMapping("/v1/items/{itemId}/price-propose")
    @ApiOperation(value = "가격 제안 요청 API", notes = "SUGGEST 상태의 가격 제안 중복 요청 불가능")
    public ApiResult<PriceProposeResponse> proposePrice(@PathVariable("itemId") Long itemId,
                                                        @RequestBody PriceProposeRequest priceProposeRequest,
                                                        HttpServletRequest request) throws IOException {

        Long userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        PriceProposeResponse priceProposeResponse = priceProposeService.proposePrice(userId, itemId, priceProposeRequest.getPrice());

        notificationService.sendMessageOfPricePropose(userId, priceProposeResponse);

        return OK(priceProposeResponse);
    }

    @DeleteMapping("/v1/items/{itemId}/price-propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 취소 요청 API", notes = "SUGGEST 상태인 가격 제안에 대해서만 취소 가능")
    public ApiResult<PriceProposeResponse> cancelPropose(@PathVariable("itemId") Long itemId,
                                                         @PathVariable("priceProposeId") Long priceProposeId,
                                                         HttpServletRequest request) {

        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.cancelPropose(userId, priceProposeId));
    }

    @PatchMapping("/v1/items/{itemId}/price-propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 제안 가격 변경 API", notes = "SUGGEST 상태인 가격 제안에 대해서만 변경 가능\n요청에 대한 처리 결과(boolean)만 반환")
    public ApiResult updatePropose(@PathVariable("itemId") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 @RequestBody PriceProposeRequest priceProposeRequest,
                                 HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.updatePropose(userId, priceProposeId, priceProposeRequest.getPrice()));
    }

    @PostMapping("/v1/items/{itemId}/price-propose/{priceProposeId}/refuse")
    @ApiOperation(value = "받은 가격 제안에 대한 거절 요청 API", notes = "요청에 대한 처리 결과(boolean)만 반환")
    public ApiResult refusePropose(@PathVariable("itemId") Long itemId,
                                   @PathVariable("priceProposeId") Long priceProposeId,
                                   HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.REFUSED));
    }

    @PostMapping("/v1/items/{itemId}/price-propose/{priceProposeId}/accept")
    @ApiOperation(value = "받은 가격 제안에 대한 수락 요청 API", notes = "요청에 대한 처리 결과(boolean)만 반환")
    public ApiResult acceptPropose(@PathVariable("itemId") Long itemId,
                                   @PathVariable("priceProposeId") Long priceProposeId,
                                   HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.ACCEPTED));
    }

    @GetMapping("/v1/items/{itemId}/price-propose")
    @ApiOperation(value = "특정 게시글에 대한 가격 제안 요청 목록 보기 API", notes = "SUGGESTED 상태인 가격 제안만 표시")
    public ApiResult<List<PriceProposeResponse>> getAllPropose(@PathVariable("itemId") Long itemId,
                                                               HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllProposeByItem(userId, itemId));
    }

    @GetMapping("/v1/items/{itemId}/price-propose/{priceProposeId}")
    @ApiOperation(value = "특정 가격 제시에 대한 상태 유효 검사", notes = "SUGGESTED/ACCEPTED 상태를 유효한 것으로 인식")
    public ApiResult checkStatusOfPricePropose(@PathVariable("itemId") Long itemId,
                                                                     @PathVariable("priceProposeId") Long priceProposeId) {
        return OK(priceProposeService.checkStatus(priceProposeId));
    }
}
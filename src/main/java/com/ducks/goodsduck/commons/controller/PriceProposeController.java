package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.ApiResult;
import com.ducks.goodsduck.commons.model.dto.PriceProposeRequest;
import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.ducks.goodsduck.commons.service.PriceProposeService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ducks.goodsduck.commons.model.dto.ApiResult.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
@Api(tags = "가격 제안 APIs")
public class PriceProposeController {

    private final PriceProposeService priceProposeService;

    @PostMapping("/item/{item_id}/propose")
    @ApiOperation(value = "가격 제안 요청 API", notes = "SUGGEST 상태의 가격 제안 중복 요청 불가능")
    public ApiResult<PriceProposeResponse> proposePrice(@PathVariable("item_id") Long itemId,
                                  @RequestBody PriceProposeRequest priceProposeRequest,
                                  HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        try {
            return OK(priceProposeService.proposePrice(userId, itemId, priceProposeRequest.getPrice())
                    .orElseThrow(() -> new RuntimeException("Cannot propose the price.")));
        } catch (IllegalAccessException e) {
            log.debug("Propose of Price API error : {}", e.getMessage(), e);
            return (ApiResult<PriceProposeResponse>) ERROR(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/item/{item_id}/propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 취소 요청 API", notes = "SUGGEST 상태인 가격 제안에 대해서만 취소 가능")
    public ApiResult<PriceProposeResponse> cancelPropose(@PathVariable("item_id") Long itemId,
                                              @PathVariable("priceProposeId") Long priceProposeId,
                                              HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        try {
            return OK(priceProposeService.cancelPropose(userId, priceProposeId)
                    .orElseThrow(() -> new RuntimeException("Cannot cancel the propose of price.")));
        } catch (IllegalAccessException e) {
            log.debug("Cancel propose API error: {}", e.getMessage(), e);
            return (ApiResult<PriceProposeResponse>) ERROR(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping("/item/{item_id}/propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 제안 가격 변경 API", notes = "SUGGEST 상태인 가격 제안에 대해서만 변경 가능\n요청에 대한 처리 결과(boolean)만 반환")
    public ApiResult updatePropose(@PathVariable("item_id") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 @RequestBody PriceProposeRequest priceProposeRequest,
                                 HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.updatePropose(userId, priceProposeId, priceProposeRequest.getPrice()));
    }

    @GetMapping("/item/{item_id}/propose/{priceProposeId}/refuse")
    @ApiOperation(value = "받은 가격 제안에 대한 수락 요청 API", notes = "요청에 대한 처리 결과(boolean)만 반환")
    public ApiResult refusePropose(@PathVariable("item_id") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.REFUSED));
    }

    @GetMapping("/item/{item_id}/propose/{priceProposeId}/accept")
    @ApiOperation(value = "받은 가격 제안에 대한 거절 요청 API", notes = "요청에 대한 처리 결과(boolean)만 반환")
    public ApiResult acceptPropose(@PathVariable("item_id") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.ACCEPTED));
    }

    @GetMapping("/item/{item_id}/propose")
    @ApiOperation(value = "특정 게시글에 대한 가격 제안 요청 목록 보기 API", notes = "SUGGESTED 상태인 가격 제안만 표시")
    public ApiResult<List<PriceProposeResponse>> getAllPropose(@PathVariable("item_id") Long itemId,
                                                    HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllProposeByItem(userId, itemId));
    }

    @GetMapping("/mypage/item/propose")
    @ApiOperation(value = "특정 유저가 받은 가격 제안 요청 목록 보기 API", notes = "SUGGESTED 상태인 가격 제안만 표시")
    public ApiResult<List<PriceProposeResponse>> getAllProposeToMe(HttpServletRequest request) {
        var userId = (Long)request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllReceiveProposeByUser(userId));
    }

    @GetMapping("/mypage/propose")
    @ApiOperation(value = "요청한 가격 제안 목록 보기 API", notes = "SUGGESTED, REFUSED 상태인 가격 제안만 표시")
    public ApiResult<List<PriceProposeResponse>> getAllProposeFromMe(HttpServletRequest request) {
        var userId = (Long) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS);
        return OK(priceProposeService.findAllGiveProposeByUser(userId));
    }

}
package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.PriceProposeRequest;
import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.ducks.goodsduck.commons.service.PriceProposeService;
import com.ducks.goodsduck.commons.util.PropertyUtil;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PriceProposeController {

    private final PriceProposeService priceProposeService;

    @PostMapping("/item/{item_id}/propose")
    @ApiOperation(value = "가격 제안 요청 API")
    public PriceProposeResponse proposePrice(@PathVariable("item_id") Long itemId,
                                             @RequestBody PriceProposeRequest priceProposeRequest,
                                             HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.proposePrice(userId, itemId, priceProposeRequest.getPrice());
    }

    @DeleteMapping("/item/{item_id}/propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 취소 요청 API")
    public PriceProposeResponse cancelPropose(@PathVariable("item_id") Long itemId,
                                              @PathVariable("priceProposeId") Long priceProposeId,
                                              HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.cancelPropose(userId, priceProposeId);
    }

    @PatchMapping("/item/{item_id}/propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 제안 가격 변경 API")
    public boolean updatePropose(@PathVariable("item_id") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 @RequestBody PriceProposeRequest priceProposeRequest,
                                 HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.updatePropose(userId, priceProposeId, priceProposeRequest.getPrice());
    }

    @GetMapping("/item/{item_id}/propose/{priceProposeId}/refuse")
    @ApiOperation(value = "받은 가격 제안에 대한 수락 요청 API")
    public boolean refusePropose(@PathVariable("item_id") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.REFUSED);
    }

    @GetMapping("/item/{item_id}/propose/{priceProposeId}/accept")
    @ApiOperation(value = "받은 가격 제안에 대한 거절 요청 API")
    public boolean acceptPropose(@PathVariable("item_id") Long itemId,
                                 @PathVariable("priceProposeId") Long priceProposeId,
                                 HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.ACCEPTED);
    }

    @GetMapping("/item/{item_id}/propose")
    @ApiOperation(value = "특정 게시글에 대한 가격 제안 요청 목록 보기 API")
    public List<PriceProposeResponse> getAllPropose(@PathVariable("item_id") Long itemId,
                                                    HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.findAllProposeByItem(userId, itemId);
    }

    @GetMapping("/mypage/item/propose")
    @ApiOperation(value = "특정 유저에 대한 가격 제안 요청 목록 보기 API")
    public List<PriceProposeResponse> getAllProposeToMe(HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.findAllReceiveProposeByUser(userId);
    }

    @GetMapping("/mypage/propose")
    @ApiOperation("요청한 가격 제안 목록 보기 API")
    public List<PriceProposeResponse> getAllProposeFromMe(HttpServletRequest request) {
        var userId = Long.valueOf((Integer) request.getAttribute(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.findAllGiveProposeByUser(userId);
    }

}

package com.ducks.goodsduck.commons.controller;

import com.ducks.goodsduck.commons.model.dto.PriceProposeRequest;
import com.ducks.goodsduck.commons.model.dto.PriceProposeResponse;
import com.ducks.goodsduck.commons.model.enums.PriceProposeStatus;
import com.ducks.goodsduck.commons.service.JwtService;
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
    private final JwtService jwtService;

    @PostMapping("/item/{item_id}/propose")
    @ApiOperation(value = "가격 제안 요청 API")
    public PriceProposeResponse proposePrice(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId, @RequestBody PriceProposeRequest priceProposeRequest) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.proposePrice(userId, itemId, priceProposeRequest.getPrice());
    }

    @DeleteMapping("/item/{item_id}/propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 취소 요청 API")
    public PriceProposeResponse cancelPropose(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId, @PathVariable("priceProposeId") Long priceProposeId) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.cancelPropose(userId, priceProposeId);
    }

    @PatchMapping("/item/{item_id}/propose/{priceProposeId}")
    @ApiOperation(value = "요청했던 가격 제안에 대한 제안 가격 변경 API")
    public boolean updatePropose(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId, @PathVariable("priceProposeId") Long priceProposeId, @RequestBody PriceProposeRequest priceProposeRequest) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.updatePropose(userId, priceProposeId, priceProposeRequest.getPrice());
    }

    @GetMapping("/item/{item_id}/propose/{priceProposeId}/refuse")
    @ApiOperation(value = "받은 가격 제안에 대한 수락 요청 API")
    public boolean refusePropose(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId, @PathVariable("priceProposeId") Long priceProposeId) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.REFUSED);
    }

    @GetMapping("/item/{item_id}/propose/{priceProposeId}/accept")
    @ApiOperation(value = "받은 가격 제안에 대한 거절 요청 API")
    public boolean acceptPropose(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId, @PathVariable("priceProposeId") Long priceProposeId) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.updateProposeStatus(userId, itemId, priceProposeId, PriceProposeStatus.ACCEPTED);
    }

    @GetMapping("/item/{item_id}/propose")
    @ApiOperation(value = "특정 게시글에 대한 가격 제안 요청 목록 보기 API")
    public List<PriceProposeResponse> getAllPropose(@RequestHeader("jwt") String jwt, @PathVariable("item_id") Long itemId) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.findAllProposeByItem(userId, itemId);
    }

    @GetMapping("/mypage/item/propose")
    @ApiOperation(value = "특정 유저에 대한 가격 제안 요청 목록 보기 API")
    public List<PriceProposeResponse> getAllProposeToMe(@RequestHeader("jwt") String jwt, HttpServletRequest request) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
//        var userId = Long.valueOf((Integer)request.getAttribute("userId"));
        return priceProposeService.findAllReceiveProposeByUser(userId);
    }

    @GetMapping("/mypage/propose")
    @ApiOperation("요청한 가격 제안 목록 보기 API")
    public List<PriceProposeResponse> getAllProposeFromMe(@RequestHeader("jwt") String jwt) {
        var userId = Long.valueOf((Integer) jwtService.getPayloads(jwt).get(PropertyUtil.KEY_OF_USERID_IN_JWT_PAYLOADS));
        return priceProposeService.findAllGiveProposeByUser(userId);
    }

}

package com.shipin.controller;
import com.shipin.entity.enums.ResponseCodeEnum;
import com.shipin.entity.vo.Result;
import com.shipin.exception.BusinessException;


public class ABaseController {

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> Result getSuccessResponseVO(T t) {
        Result<T> responseVO = new Result<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setMsg(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> Result getBusinessErrorResponseVO(BusinessException e, T t) {
        Result vo = new Result();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setMsg(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> Result getServerErrorResponseVO(T t) {
        Result vo = new Result();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setMsg(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }
}

package cn.eroad.controller;

import cn.eroad.core.domain.CommonContent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @date 2022/1/6 23:42
 */
@RestController
public class RedirectController {
    @GetMapping(value = "/400")
    public CommonContent R400() {
        return CommonContent.error("40*错误");
    }

    @GetMapping(value = "/500")
    public CommonContent R500() {
        return CommonContent.error("50*错误");
    }
}

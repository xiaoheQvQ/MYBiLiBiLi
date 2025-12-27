package com.hsx.manyue.modules.apply;

import cn.hutool.json.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.JwtUtil;
import com.hsx.manyue.modules.apply.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AliPayController {

    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";

    @Resource
    private AliPayConfig aliPayConfig;

    @Autowired
    private BalanceService balanceService;

    @GetMapping("/pay") // &subject=xxx&traceNo=xxx&totalAmount=xxx
    public void pay(AliPay aliPay,          // 自动接收traceNo、totalAmount、subject
                    HttpServletResponse httpResponse) throws Exception {
        AlipayClient alipayClient = new DefaultAlipayClient(GATEWAY_URL, aliPayConfig.getAppId(),
                aliPayConfig.getAppPrivateKey(), FORMAT, CHARSET, aliPayConfig.getAlipayPublicKey(), SIGN_TYPE);

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(aliPayConfig.getNotifyUrl());
        request.setReturnUrl(aliPayConfig.getReturnUrl()+"1909974821607518210?nick=呼呼呼风&live_from=71005&visit_id=33llw6rlnmm0");

        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", aliPay.getTraceNo());
        bizContent.set("total_amount", aliPay.getTotalAmount());
        bizContent.set("subject", aliPay.getSubject());
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = "";
        try {
            form = alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        httpResponse.setContentType("text/html;charset=" + CHARSET);
        httpResponse.getWriter().write(form);
        httpResponse.getWriter().flush();
        httpResponse.getWriter().close();
    }

    @PostMapping("/notify")
    public String payNotify(
                             HttpServletRequest request) throws Exception {

        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            System.out.println("=========支付宝异步回调========");

            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }

            String outTradeNo = params.get("out_trade_no");
            String totalAmount = params.get("total_amount");
            String alipayTradeNo = params.get("trade_no");

            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, aliPayConfig.getAlipayPublicKey(), "UTF-8");

            if (checkSignature) {

                int lastUnderscoreIndex = outTradeNo.lastIndexOf("_");
                String userId = outTradeNo.substring(lastUnderscoreIndex + 1);
                System.out.println("9999999999999999");
                System.out.println("userId"+userId+":"+totalAmount+":"+alipayTradeNo);
                balanceService.recharge(Long.valueOf(userId), new BigDecimal(totalAmount), alipayTradeNo);
                System.out.println("88888888888");
            }
        }
        return "success";
    }

    @GetMapping("/return")
    public R returnPay(@RequestParam String out_trade_no,
                       @RequestParam String total_amount,
                       HttpServletResponse response) throws Exception {
        // 这里可以处理支付成功后的逻辑，比如更新订单状态
        return R.success("支付成功" );
    }

    @GetMapping("/create-order")
    public R createOrder(
                         @RequestParam BigDecimal amount,
                         @RequestParam String subject) {
        Long userId = JwtUtil.LOGIN_USER_HANDLER.get();
        String orderId = "RECHARGE_" + System.currentTimeMillis() + "_" + userId;
        return R.success(Map.of(
                "orderId", orderId,
                "userId", userId,
                "amount", amount,
                "subject", subject
        ));
    }
}
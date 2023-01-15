package com.atguigu.yygh.gateway.filter;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

//@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {
    private AntPathMatcher antPathMatcher=new AntPathMatcher();
    //执行过滤功能
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        //对于登录接口的请求就不要拦截了
        if (antPathMatcher.match("/admin/user/**",path)){
            return chain.filter(exchange);
        }else {//对于非登录接口，验证，必须登录之后才能通过
            List<String> strings = request.getHeaders().get("X-Token");
              if (strings==null){
                  ServerHttpResponse response = exchange.getResponse();
                   response.setStatusCode(HttpStatus.SEE_OTHER);
                   //路由跳转
                  response.getHeaders().set(HttpHeaders.LOCATION,"http://localhost:9528");

                  return response.setComplete();//结束请求
              }else {//放行
                  return chain.filter(exchange);

              }
        }

    }
    //影响的是全局过滤器的执行顺序，值越小优先级越高
    @Override
    public int getOrder() {
        return 0;
    }
}

package com.movie.gateway.filter;

import com.movie.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITELIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/movies",
            "/api/categories",
            "/api/actors",
            "/api/directors",
            "/api/reviews/movie",
            "/api/reviews/user",
            "/api/rankings"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单路径放行
        for (String white : WHITELIST) {
            if (path.startsWith(white)) {
                return chain.filter(exchange);
            }
        }

        // 公开 GET 请求放行（除需要登录的个人路径外）
        if (exchange.getRequest().getMethod() == HttpMethod.GET
                && !path.startsWith("/api/admin")
                && !path.startsWith("/api/users/me")
                && !path.startsWith("/api/watchlist/me")
                && !(path.startsWith("/api/watchlist/movie/") && path.endsWith("/my-status"))) {
            return chain.filter(exchange);
        }

        // 提取并验证 JWT
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "未提供认证令牌");
        }

        try {
            Claims claims = JwtUtil.parseToken(authHeader.substring(7));

            // 管理员接口必须 ADMIN 角色
            if (path.startsWith("/api/admin") && !JwtUtil.isAdmin(claims)) {
                return forbidden(exchange, "需要管理员权限");
            }

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(JwtUtil.getUserId(claims)))
                    .header("X-Username", JwtUtil.getUsername(claims))
                    .header("X-User-Role", JwtUtil.getRole(claims))
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (Exception e) {
            return unauthorized(exchange, "令牌无效或已过期");
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String body = "{\"code\":403,\"message\":\"" + message + "\",\"data\":null}";
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

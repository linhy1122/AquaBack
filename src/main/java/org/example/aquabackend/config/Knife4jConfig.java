package org.example.aquabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class Knife4jConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SPRING_WEB)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.example.aquabackend.controller"))
                .paths(PathSelectors.any())
                .build()
                // 配置全局 JWT 认证
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("AquaBackEnd - 水产养殖管理系统 API 文档")
                .description(" ## AquaBackEnd 后端接口文档 ### 认证方式 - 公开接口（/api/auth/**）：无需认证 - 管理接口（/api/admin/**）：需要 ADMIN 角色 + JWT Token - 其他接口：需要登录（JWT Token） ### 使用说明 1. 先调用 `/api/auth/captcha` 获取验证码 2. 调用 `/api/auth/login` 登录获取 JWT Token 3. 在右上角「Authorize」按钮填入 Token（格式：Bearer xxx） 4. 调用其他受保护的接口 ")
                .version("2.0")
                .contact(new Contact("AquaTeam", "", "admin@aqua.com"))
                .build();
    }

    /**
     * 配置全局安全方案（JWT Bearer Token）
     */
    private List<SecurityScheme> securitySchemes() {
        return Collections.singletonList(
                new ApiKey("Authorization", "Authorization", "header")
        );
    }

    /**
     * 配置安全上下文（哪些路径需要 Token）
     */
    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(securityReference())
                        .forPaths(PathSelectors.regex("^(?!/api/auth/).*$"))
                        .build()
        );
    }

    private List<SecurityReference> securityReference() {
        return Collections.singletonList(
                new SecurityReference("Authorization", new AuthorizationScope[]{
                        new AuthorizationScope("global", "accessEverything")
                })
        );
    }
}

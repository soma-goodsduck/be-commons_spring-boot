package com.ducks.goodsduck.commons.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * URI : /swagger-ui.html
 * 사용 예시) 컨트롤러 메서드에 다음과 같은 어노테이션을 추가한다.
 * @ApiOperation(value = "사용자 정보등록" , notes = "상세한 사용자 정보에 대해서 출력한다.")
 * @ApiImplicitParams({
 *         @ApiImplicitParam(name = "email", value = "사용자의 이메일을 입력한다", required = false, dataType = "SearchVO", paramType = "string", defaultValue = ""),
 *         @ApiImplicitParam(name = "id", value = "사용자의 id값", required = false, dataType = "SearchVO", paramType = "string", defaultValue = ""),
 *         @ApiImplicitParam(name = "page", value = "페이지 숫자", required = false, dataType = "SearchVO", paramType = "int", defaultValue = ""),
 * })
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket restApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ducks.goodsduck.commons"))
                .paths(PathSelectors.ant("/api/v1/**"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("GoodsDuck Api")
                .contact(new Contact("Team Ducks", "https://www.notion.so/Ducks-4e0270bfe9714d3dbd3f511ce7de311d", "ting_916@naver.com"))
                .version("1.0.0")
                .description("Backend Api description for GoodsDuck")
                .build();
    }

}

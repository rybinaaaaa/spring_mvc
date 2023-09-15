package rybina.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;


/**
 * Данный класс заменяет файл applicationContextMVC.xml
 * внутри этого кдасса мы переписывем ниже указанные (в комментарии под классом бины)
 */
@Configuration
@ComponentScan("rybina")
@EnableWebMvc
public class SpringConfig  implements WebMvcConfigurer {

    private final ApplicationContext applicationContext;

    @Autowired
    public SpringConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("/WEB-INF/views/");
        templateResolver.setSuffix(".html");
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        registry.viewResolver(resolver);
    }
}

//<bean id="templateResolver" class="org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver">
//<property name="prefix" value="/WEB-INF/views/"/>
//<property name="suffix" value=".html"/>
//</bean>
//
//<bean id="templateEngine" class="org.thymeleaf.spring5.SpringTemplateEngine">
//<property name="templateResolver" ref="templateResolver"/>
//<property name="enableSpringELCompiler" value="true"/>
//</bean>
//
//<bean class="org.thymeleaf.spring5.view.ThymeleafViewResolver">
//<property name="templateEngine" ref="templateEngine"/>
//<property name="order" value="1"/>
//<property name="viewNames" value="*"/>
//</bean>
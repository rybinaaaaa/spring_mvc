package rybina.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * замена web.xml
 */
public class mySpringMvcDispatcherInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] {SpringConfig.class}; // мы возвращаем массив с 1 элементом типа class
    }
// equal to
//  <servlet>
//    <servlet-name>dispatcher</servlet-name>
//    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
//    <init-param>
//      <param-name>contextConfigLocation</param-name>
//      <param-value>/WEB-INF/applicationContextMVC.xml</param-value>
//    </init-param>
//    <load-on-startup>1</load-on-startup>
//  </servlet>

    @Override
    protected String[] getServletMappings() {
        return new String[] {"/"};
    }
//  equal to
//  <servlet-mapping>
//             <servlet-name>dispatcher</servlet-name>
//             <url-pattern>/</url-pattern>
//    </servlet-mapping>
}

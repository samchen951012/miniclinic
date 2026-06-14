package tw.edu.fju.miniclinic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tw.edu.fju.miniclinic.interceptor.LoginRequiredInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginRequiredInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns(
                "/dashboard",
                "/dashboard/**",
                "/api/auth/me",
                "/api/appointments/*/status",
                "/password"
            )
            .excludePathPatterns(
                "/login",
                "/logout",
                "/stats",     // ✨ 排除網頁路由攔截，允許公開訪問
                "/api/stats"  // ✨ 排除統計 API 攔截，允許公開免登入訪問
            );
    }
}
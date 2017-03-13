package edu.hneu.studentsportal.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

import javax.annotation.Resource
import javax.servlet.Filter

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    Filter ssoFilter

    @Override
    void configure(final HttpSecurity http) {
        http.authorizeRequests()
                .antMatchers('/management/**')
                    .access("hasRole('ROLE_ADMIN')")
                .antMatchers('/account/**')
                    .access("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
                .and()
                    .formLogin()
                        .loginPage('/login')
                        .defaultSuccessUrl('/account', false)
                        .failureUrl('/login?error=true')
                .and()
                    .logout()
                        .logoutSuccessUrl('/login')
                .and()
                    .addFilterBefore(ssoFilter, BasicAuthenticationFilter.class)
                        .exceptionHandling()
                            .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint('/'))
                .and()
                    .csrf()
                        .disable()
    }

}
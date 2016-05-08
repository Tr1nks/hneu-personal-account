package com.rozdolskyi.traininghneu.controller;

import com.rozdolskyi.traininghneu.model.StudentProfile;
import com.rozdolskyi.traininghneu.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Optional;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private StudentService studentService;

    @RequestMapping
    public ModelAndView account() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (hasAdminRole(auth.getAuthorities()))
            return new ModelAndView("redirect:management/uploadStudentProfilesFromExcel");
        return getUserProfileView(auth.getName());
    }

    private boolean hasAdminRole(Collection<? extends GrantedAuthority> authorities) {
        return authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private ModelAndView getUserProfileView(String email) {
        Optional<StudentProfile> studentProfile = studentService.findStudentProfileByEmail(email);
        if (studentProfile.isPresent())
            return new ModelAndView("account", "profile", studentProfile.get());
        return new ModelAndView("redirect:login");
    }

}
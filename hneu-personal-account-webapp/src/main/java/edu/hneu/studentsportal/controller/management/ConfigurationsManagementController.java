package edu.hneu.studentsportal.controller.management;

import edu.hneu.studentsportal.enums.UserRole;
import edu.hneu.studentsportal.feature.SiteFeature;
import edu.hneu.studentsportal.service.UserService;
import javaslang.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

import static edu.hneu.studentsportal.controller.ControllerConstants.MANAGE_CONFIGURATIONS_URL;
import static java.util.Objects.nonNull;

@Log4j
@Controller
@RequestMapping(MANAGE_CONFIGURATIONS_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationsManagementController extends AbstractManagementController {

    private final UserService userService;

    @GetMapping
    public String getConfigs(Model model) {
        model.addAttribute("admins", userService.getAdmins());
        model.addAttribute("title", "settings");
        Stream.of(SiteFeature.values()).forEach(feature -> model.addAttribute(feature.name(), feature.isActive()));
        return "management/configurations-page";
    }

    @PostMapping("/email-sending")
    public String saveEmailConfigs(HttpServletRequest request) {
        changeFeatureStateIfNeeded(request, SiteFeature.SEND_EMAIL_AFTER_PROFILE_CREATION.name(), SiteFeature.SEND_EMAIL_AFTER_PROFILE_CREATION);
        changeFeatureStateIfNeeded(request, SiteFeature.SEND_EMAIL_AFTER_PROFILE_MODIFICATION.name(), SiteFeature.SEND_EMAIL_AFTER_PROFILE_MODIFICATION);
        return "redirect:" + MANAGE_CONFIGURATIONS_URL;
    }

    @PostMapping("/account-features")
    public String saveAccountFeaturesConfigs(HttpServletRequest request) {
        changeFeatureStateIfNeeded(request, SiteFeature.VIEW_SCHEDULE.name(), SiteFeature.VIEW_SCHEDULE);
        changeFeatureStateIfNeeded(request, SiteFeature.VIEW_CURRENT_MARKS.name(), SiteFeature.VIEW_CURRENT_MARKS);
        changeFeatureStateIfNeeded(request, SiteFeature.VIEW_DOCUMENTS.name(), SiteFeature.VIEW_DOCUMENTS);
        changeFeatureStateIfNeeded(request, SiteFeature.SEND_EMAIL_TO_DECAN.name(), SiteFeature.SEND_EMAIL_TO_DECAN);
        changeFeatureStateIfNeeded(request, SiteFeature.LOAD_PROFILE_PDF.name(), SiteFeature.LOAD_PROFILE_PDF);
        return "redirect:" + MANAGE_CONFIGURATIONS_URL;
    }

    private void changeFeatureStateIfNeeded(HttpServletRequest request, String name, SiteFeature feature) {
        boolean flag = nonNull(request.getParameter(name));
        if (flag != feature.isActive()) {
            feature.toggle();
            log.info(String.format("Configuration [%s] is changed, new value is [%s]", feature.toString(), feature.isActive()));
        }
    }

    @PostMapping("/admins")
    public String addAdmin(HttpServletRequest request) {
        String email = request.getParameter("email");
        userService.create(email, UserRole.ADMIN);
        log.info(String.format("New admin [%s] has been added", email));
        return "redirect:" + MANAGE_CONFIGURATIONS_URL;
    }

    @PostMapping("/admins/{email}/delete")
    public String delete(@PathVariable String email, RedirectAttributes redirectAttributes) {
        Try.run(() -> userService.delete(email)).onFailure(e ->
                redirectAttributes.addFlashAttribute("error", messageService.cannotDeleteAdmin()));
        return "redirect:" + MANAGE_CONFIGURATIONS_URL;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleError(DataIntegrityViolationException e, RedirectAttributes redirectAttributes) {
        return handleErrorInternal(e, messageService.adminExistsError(), redirectAttributes);
    }

    @Override
    public String baseUrl() {
        return MANAGE_CONFIGURATIONS_URL;
    }

    @Override
    public Logger logger() {
        return log;
    }
}
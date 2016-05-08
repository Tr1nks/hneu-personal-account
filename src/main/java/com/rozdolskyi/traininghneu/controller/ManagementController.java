package com.rozdolskyi.traininghneu.controller;

import com.rozdolskyi.traininghneu.model.FilesUploadModel;
import com.rozdolskyi.traininghneu.model.StudentProfile;
import com.rozdolskyi.traininghneu.model.User;
import com.rozdolskyi.traininghneu.service.FileService;
import com.rozdolskyi.traininghneu.service.StudentService;
import com.rozdolskyi.traininghneu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/management")
public class ManagementController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/uploadStudentProfilesFromExcel")
    public String addUserProfilesFromExcel() {
        return "uploadUserProfilesFromExcel";
    }

    @RequestMapping(value = "/uploadStudentProfilesFromExcel", method = RequestMethod.POST)
    public String uploadFiles(@ModelAttribute("uploadForm") FilesUploadModel uploadForm,
                              RedirectAttributes redirectAttributes) throws IllegalStateException, IOException {
        List<MultipartFile> filesToUpload = uploadForm.getFiles();
        String filePath = servletContext.getRealPath("/WEB-INF/excel/uploaded");
        Map<String, Boolean> uploadedFilesNames = fileService.reduceForEachUploadedFile(filesToUpload, filePath, uploadedFile -> {
                    StudentProfile studentProfile = studentService.readStudentProfilesFromFile(uploadedFile);
                    studentService.setCredentials(studentProfile);
                    studentService.save(studentProfile);
        });
        redirectAttributes.addFlashAttribute("files", uploadedFilesNames);
        return "redirect:successfullyUploaded";
    }

    @RequestMapping(value = "/uploadTotalsFromExcel", method = RequestMethod.POST)
    public String uploadTotalsFromExcel(@ModelAttribute("uploadForm") FilesUploadModel uploadForm,
                                        RedirectAttributes redirectAttributes) throws IllegalStateException, IOException {
        List<MultipartFile> filesToUpload = uploadForm.getFiles();
        String filePath = servletContext.getRealPath("/WEB-INF/excel/uploaded");
        Map<String, Boolean> uploadedFilesNames = fileService.reduceForEachUploadedFile(filesToUpload, filePath,
                uploadedFile -> studentService.updateStudentsScoresFromFile(uploadedFile)
        );
        redirectAttributes.addFlashAttribute("files", uploadedFilesNames);
        return "redirect:successfullyUploaded";
    }

    @RequestMapping(value = "/uploadTotalsFromExcel")
    public String uploadTotalsFromExcel() {
        return "uploadTotalsFromExcel";
    }

    @RequestMapping(value = "/successfullyUploaded", method = RequestMethod.GET)
    public String successfullyUploaded() {
        return "successfullyUploaded";
    }

}
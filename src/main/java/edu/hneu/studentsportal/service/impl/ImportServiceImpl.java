package edu.hneu.studentsportal.service.impl;


import edu.hneu.studentsportal.entity.DisciplineMark;
import edu.hneu.studentsportal.entity.Student;
import edu.hneu.studentsportal.enums.UserRole;
import edu.hneu.studentsportal.parser.factory.ParserFactory;
import edu.hneu.studentsportal.repository.StudentRepository;
import edu.hneu.studentsportal.service.ImportService;
import edu.hneu.studentsportal.service.UserService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.lang.String.format;

@Log4j
@Service
public class ImportServiceImpl implements ImportService {

    @Resource
    private ParserFactory parserFactory;
    @Resource
    private UserService userService;
    @Resource
    private StudentRepository studentRepository;

    @Value("${emails.integration.service.url}")
    public String emailsIntegrationServiceUrl;

    @Override
    public Student importStudent(File file) {
        Student student = parserFactory.newStudentProfileExcelParser().parse(file);
        //student.setEmail(retrieveStudentEmailFromThirdPartyService(student));
        student.setEmail("oleksandr.rozdolskyi@hneu.net");
        userService.create(student.getEmail(), UserRole.STUDENT);
        studentRepository.save(student);
        log.info(format("New %s has been created.", student));
        return student;
    }

    @Override
    public Set<Student> importStudentMarks(File file) {
        Map<Student, List<DisciplineMark>> importedStudentsMarks = parserFactory.newStudentMarksExcelParser().parse(file);
        importedStudentsMarks.forEach((student, importedMarks) -> {
            importedMarks.forEach(merge(student.getDisciplineMarks()));
            studentRepository.save(student);
        });
        return importedStudentsMarks.keySet();
    }

    private Consumer<DisciplineMark> merge(List<DisciplineMark> existingMarks) {
        return importedMark -> {
            DisciplineMark studentDiscipline = alignWithExisting(importedMark, existingMarks);
            studentDiscipline.setMark(importedMark.getMark());
        };
    }

    private DisciplineMark alignWithExisting(DisciplineMark importedMark, List<DisciplineMark> existingMarks) {
        return existingMarks.stream()
                .filter(sm -> sm.getDiscipline().equals(importedMark.getDiscipline()))
                .findFirst().orElse(importedMark);
    }

    private String retrieveStudentEmailFromThirdPartyService(Student student) {
        String formattedName = student.getName().toLowerCase().split(" ")[0];
        String formatterSurname = student.getSurname().toLowerCase().trim();
        String groupName = student.getGroup().getName();
        String url = format("%s/EmailToOutController?name=%s&surname=%s&groupId=%s", emailsIntegrationServiceUrl, formattedName, formatterSurname, groupName);
        return new RestTemplate().getForEntity(url, String.class).getBody().toLowerCase();
    }

}
package edu.hneu.studentsportal.service.impl;

import edu.hneu.studentsportal.domain.Discipline;
import edu.hneu.studentsportal.domain.DisciplineMark;
import edu.hneu.studentsportal.domain.EducationProgram;
import edu.hneu.studentsportal.domain.Speciality;
import edu.hneu.studentsportal.domain.Student;
import edu.hneu.studentsportal.repository.DisciplineRepository;
import edu.hneu.studentsportal.service.StudentDisciplineMarksService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.BooleanUtils.isFalse;

@Service
public class StudentDisciplineMarksServiceImpl implements StudentDisciplineMarksService {

    @Resource
    private DisciplineRepository disciplineRepository;

    @Override
    public List<DisciplineMark> getStudentMarks(Student student, int course, int semester) {
        Predicate<DisciplineMark> hasGivenCourse = m -> m.getDiscipline().getCourse() == course;
        Predicate<DisciplineMark> hasGivenSemester = m -> m.getDiscipline().getSemester() == semester;
        return student.getDisciplineMarks().stream()
                .filter(m -> nonNull(m.getDiscipline()))
                .filter(hasGivenCourse.and(hasGivenSemester)).collect(toList());
    }

    @Override
    public List<Discipline> getPossibleNewDisciplinesForStudent(Student student, int course, int semester) {
        Speciality speciality = student.getSpeciality();
        EducationProgram educationProgram = student.getEducationProgram();
        List<Discipline> exceptDisciplines = extract(getStudentMarks(student, course, semester), DisciplineMark::getDiscipline);
        Predicate<Discipline> wasNotPickedForStudent = discipline -> isFalse(exceptDisciplines.contains(discipline));
        return disciplineRepository.findByCourseAndSemesterAndSpecialityAndEducationProgram(course, semester, speciality, educationProgram)
                .stream()
                .filter(wasNotPickedForStudent)
                .collect(toList());
    }

    @Override
    public List<Integer> getStudentCourses(Student student) {
        return extract(student.getDisciplineMarks(), m -> m.getDiscipline().getCourse()).stream().distinct().collect(toList());
    }

    @Override
    public <E> List<E> extract(Collection<DisciplineMark> marks, Function<DisciplineMark, E> extractor) {
        return marks.stream().filter(m -> nonNull(m.getDiscipline())).map(extractor).collect(toList());
    }
}

package edu.hneu.studentsportal.service;

import edu.hneu.studentsportal.conditions.DisciplineConditions;
import edu.hneu.studentsportal.domain.Discipline;
import edu.hneu.studentsportal.domain.DisciplineMark;
import edu.hneu.studentsportal.domain.Student;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Service
public class DisciplineMarkService {

    public List<DisciplineMark> getStudentMarks(Student student, Integer course) {
        Predicate<DisciplineMark> hasGivenCourse = m -> Objects.equals(m.getDiscipline().getCourse(), course);
        return student.getDisciplineMarks().stream()
                .filter(m -> nonNull(m.getDiscipline()))
                .filter(hasGivenCourse).collect(toList());
    }

    public List<Integer> getStudentCourses(Student student) {
        return extract(student.getDisciplineMarks(), m -> m.getDiscipline().getCourse()).stream().distinct().collect(toList());
    }


    public List<DisciplineMark> alignStudentDisciplinesMark(Student student, List<DisciplineMark> disciplineMarks) {
        List<Discipline> allStudentMagmaynors = extractMagmaynors(student.getDisciplineMarks());
        IntStream.range(0, disciplineMarks.size()).forEach(i -> {
            DisciplineMark disciplineMark = disciplineMarks.get(i);
            Discipline discipline = disciplineMark.getDiscipline();
            if (DisciplineConditions.isMasterDisciplineLabel(discipline.getLabel())) {
                Integer number = Integer.valueOf(discipline.getLabel().split(" ")[1]);
                List<Discipline> magmaynersPerSemester = allStudentMagmaynors.stream()
                        .filter(d -> d.getSemester().equals(discipline.getSemester()))
                        .filter(d -> d.getCourse().equals(discipline.getCourse()))
                        .collect(toList());
                if (number <= magmaynersPerSemester.size())
                    disciplineMark.setDiscipline(magmaynersPerSemester.get(number - 1));
            }
        });
        return disciplineMarks.stream().filter(m -> nonNull(m.getDiscipline().getSemester())).collect(toList());
    }

    public List<DisciplineMark> updateStudentMarks(Student student, List<DisciplineMark> importedMarks) {
        return importedMarks.stream()
                .map(importedMark -> merge(importedMark, student.getDisciplineMarks()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(mark -> ObjectUtils.notEqual(mark.getMark(), mark.getPreviousMark()))
                .collect(toList());
    }

    public <E> List<E> extract(Collection<DisciplineMark> marks, Function<DisciplineMark, E> extractor) {
        return marks.stream().filter(m -> nonNull(m.getDiscipline())).map(extractor).collect(toList());
    }

    private Optional<DisciplineMark> merge(DisciplineMark importedMark, List<DisciplineMark> existingMarks) {
        Function<DisciplineMark, DisciplineMark> mergeExistedAndUpdatedMarks = mark -> {
            mark.setPreviousMark(mark.getMark());
            mark.setMark(importedMark.getMark());
            return mark;
        };
        return getPreviousDisciplineMark(importedMark, existingMarks).map(mergeExistedAndUpdatedMarks);
    }

    private Optional<DisciplineMark> getPreviousDisciplineMark(DisciplineMark importedMark, List<DisciplineMark> existingMarks) {
        return existingMarks.stream()
                .filter(sm -> disciplinesEquals(importedMark.getDiscipline(), sm.getDiscipline()))
                .findFirst();
    }

    private boolean disciplinesEquals(Discipline importedDiscipline, Discipline existingDiscipline) {
        return existingDiscipline.getCode().equals(importedDiscipline.getCode())
                && existingDiscipline.getCourse().equals(importedDiscipline.getCourse())
                && existingDiscipline.getSemester().equals(importedDiscipline.getSemester());
    }

    private List<Discipline> extractMagmaynors(List<DisciplineMark> disciplineMarks) {
        List<Discipline> studentDisciplines = extract(disciplineMarks, DisciplineMark::getDiscipline);
        return studentDisciplines.stream().filter(DisciplineConditions::isMasterDiscipline).collect(toList());
    }

    public Map<Integer, List<DisciplineMark>> getStudentMarksGroupedBySemester(List<DisciplineMark> marks) {
        Function<DisciplineMark, Integer> extractSemester = m -> m.getDiscipline().getSemester();
        return marks.stream().collect(Collectors.groupingBy(extractSemester));
    }
}

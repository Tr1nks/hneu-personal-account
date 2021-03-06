package edu.hneu.studentsportal.parser.impl;


import com.google.common.collect.Lists;
import edu.hneu.studentsportal.domain.Discipline;
import edu.hneu.studentsportal.domain.EducationProgram;
import edu.hneu.studentsportal.domain.Speciality;
import edu.hneu.studentsportal.enums.DisciplineFormControl;
import edu.hneu.studentsportal.enums.DisciplineType;
import edu.hneu.studentsportal.parser.AbstractExcelParser;
import edu.hneu.studentsportal.parser.Indexer;
import edu.hneu.studentsportal.repository.EducationProgramRepository;
import edu.hneu.studentsportal.repository.SpecialityRepository;
import javaslang.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Log4j
@Component
@Scope("prototype")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DisciplinesExcelParser extends AbstractExcelParser<List<Discipline>> {

    private final SpecialityRepository specialityRepository;
    private final EducationProgramRepository educationProgramRepository;

    @Override
    public List<Discipline> extractModel() {
        List<Discipline> disciplines = Lists.newArrayList();
        Indexer indexer = Indexer.of(0);

        validateDisciplinesExcelFile(indexer.getValue());

        while (isNotFileEnd(indexer.next())) {
            Try.of(() -> parseDiscipline(indexer))
                    .andThen(disciplines::add)
                    .onFailure(e -> log.warn(e.getMessage(), e));
        }
        return disciplines;
    }

    private void validateDisciplinesExcelFile(int row) {
        validateHeaders(row, newArrayList("Спеціальність", "Спеціалізація", "Тип", "Код",
                "Назви навчальних  дисциплін", "Кількість кредитів ЄКТС", "Курс", "Семестр", "Форма контроля"));
    }

    private boolean isNotFileEnd(int row) {
        return isFalse(isEmpty(getString1CellValue(row)));
    }

    private Discipline parseDiscipline(Indexer indexer) {
        return Discipline.builder()
                .speciality(parseSpeciality(indexer))
                .educationProgram(parseEducationProgram(indexer))
                .type(DisciplineType.of(getStringCellValue(indexer, 2)))
                .code(getStringCellValue(indexer, 3))
                .label(getStringCellValue(indexer, 4))
                .credits(getIntegerCellValue(indexer, 5))
                .course(getIntegerCellValue(indexer, 6))
                .semester(getIntegerCellValue(indexer, 7))
                .controlForm(DisciplineFormControl.of(getStringCellValue(indexer, 8)))
                .build();
    }

    private Speciality parseSpeciality(Indexer indexer) {
        String code = getStringCellValue(indexer,0);
        return specialityRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Cannot find speciality for code: " + code));
    }

    private EducationProgram parseEducationProgram(Indexer indexer) {
        String code = getStringCellValue(indexer, 1);
        return educationProgramRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Cannot find education program for code: " + code));
    }

}

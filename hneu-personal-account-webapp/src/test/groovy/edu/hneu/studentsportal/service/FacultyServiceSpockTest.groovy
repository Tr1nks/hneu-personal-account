package edu.hneu.studentsportal.service

import edu.hneu.studentsportal.domain.Faculty
import edu.hneu.studentsportal.repository.FacultyRepository
import edu.hneu.studentsportal.repository.SpecialityRepository
import spock.lang.Specification

class FacultyServiceSpockTest extends Specification {

    static final int FACULTY_ID = 1

    def facultyRepositoryMock = Mock(FacultyRepository)
    def specialityRepositoryMock = Mock(SpecialityRepository)
    def facultyMock = Mock(Faculty)

    def facultyService = new FacultyService(facultyRepositoryMock, specialityRepositoryMock)

    def 'should find faculty with specialities'() {
        given:
            facultyRepositoryMock.findById(FACULTY_ID) >> Optional.of(facultyMock)
            facultyMock.getId() >> FACULTY_ID
            specialityRepositoryMock.checkFacultyHasSpecialities(FACULTY_ID) >> Optional.of(BigInteger.valueOf(FACULTY_ID))
        when:
            def faculty = facultyService.findByIdWithSpecialitiesOrDefault(FACULTY_ID)
        then:
            facultyMock == faculty
    }

    def 'should find first faculty with specialities when expected faculty does not have specialities'() {
        given:
            facultyRepositoryMock.findById(FACULTY_ID) >> Optional.of(facultyMock)
            facultyMock.getId() >> FACULTY_ID
            specialityRepositoryMock.checkFacultyHasSpecialities(FACULTY_ID) >> Optional.empty()
            specialityRepositoryMock.findFirstFacultyIdWithSpecialities() >> Optional.of(BigInteger.valueOf(FACULTY_ID))
            facultyRepositoryMock.findOne(FACULTY_ID) >> facultyMock
        when:
            def faculty = facultyService.findByIdWithSpecialitiesOrDefault(FACULTY_ID)
        then:
            facultyMock == faculty
    }

    def 'should find first faculty with specialities when expected faculty was not found'() {
        given:
            facultyRepositoryMock.findById(FACULTY_ID) >> Optional.empty()
            specialityRepositoryMock.findFirstFacultyIdWithSpecialities() >> Optional.of(BigInteger.valueOf(FACULTY_ID))
            facultyRepositoryMock.findOne(FACULTY_ID) >> facultyMock
        when:
            def faculty = facultyService.findByIdWithSpecialitiesOrDefault(FACULTY_ID)
        then:
            facultyMock == faculty
    }

}

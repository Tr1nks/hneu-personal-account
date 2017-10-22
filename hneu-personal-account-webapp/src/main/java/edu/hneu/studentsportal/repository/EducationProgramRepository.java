package edu.hneu.studentsportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hneu.studentsportal.domain.EducationProgram;
import edu.hneu.studentsportal.domain.Speciality;

@Repository
public interface EducationProgramRepository extends JpaRepository<EducationProgram, Long> {

    Optional<EducationProgram> findByName(String name);

    List<EducationProgram> findAllBySpeciality(Speciality speciality);

    EducationProgram findById(Long id);

}
package edu.hneu.studentsportal.service;

import edu.hneu.studentsportal.domain.Student;

import java.io.File;
import java.util.Set;

public interface ImportService {

    Student importStudent(File file);

    Set<Student> importStudentMarks(File file);

}

package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.exception.ConflictException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SchoolClassDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SchoolClassService {
    private final SchoolClassRepository schoolClassRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository) {
        this.schoolClassRepository = schoolClassRepository;
    }

    // Finds all school classes
    public List<SchoolClass> findAllSchoolClasses() {
        return schoolClassRepository.findAll();
    }

    // Finds a school class with given ID
    public SchoolClass findSchoolClassById(Long id) {
        return schoolClassRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School class with id = "+id+" not found"));
    }

    // Add a new school class with request DTO
    public SchoolClass addSchoolClass(SchoolClassDto dto) {
        if(schoolClassRepository.existsByName(dto.getName())) {
            throw new ConflictException("School class '" + dto.getName() + "' already exists");
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setName(dto.getName());

        return schoolClassRepository.save(schoolClass);
    }

    // Update a school class with given ID
    public SchoolClass updateSchoolClass(SchoolClassDto dto, Long id) throws ResourceNotFoundException {
        Optional<SchoolClass> optionalSchoolClass = schoolClassRepository.findById(id);

        // If school class exists, update it
        if(optionalSchoolClass.isPresent()) {
            SchoolClass schoolClass = new SchoolClass();

            schoolClass.setId(id);
            schoolClass.setCreatedAt(optionalSchoolClass.get().getCreatedAt());
            schoolClass.setName(dto.getName());
            return schoolClassRepository.save(schoolClass);
        } else {
            throw new ResourceNotFoundException("School class with id=" + id + " not found");
        }
    }

    // Delete a school class with given ID
    @Transactional
    public void deleteSchoolClass(Long id) {
        SchoolClass schoolClass = schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School class with id=" + id + " not found"));

        List<String> dependencies = new ArrayList<>();
        if (schoolClass.getTeacher() != null) {
            dependencies.add("teacher");
        }
        if (!schoolClass.getStudents().isEmpty()) {
            dependencies.add("students");
        }
        if (!schoolClass.getTests().isEmpty()) {
            dependencies.add("tests");
        }

        if (!dependencies.isEmpty()) {
            throw new ConflictException(
                    "Cannot delete school class with id=" + id
                            + " because it has related " + String.join(", ", dependencies)
            );
        }

        schoolClassRepository.delete(schoolClass);
    }
}

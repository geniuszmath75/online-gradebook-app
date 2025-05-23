package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestDto;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestUpdateDto;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeTestService {
    private final KnowledgeTestRepository knowledgeTestRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public KnowledgeTestService(KnowledgeTestRepository knowledgeTestRepository,
                                SchoolClassRepository classRepository,
                                SubjectRepository subjectRepository,
                                UserRepository userRepository) {
        this.knowledgeTestRepository = knowledgeTestRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    // Find all knowledge tests
    public List<KnowledgeTest> findAllKnowledgeTests() {
        return knowledgeTestRepository.findAll();
    }

    // Find knowledge test with given ID
    public KnowledgeTest findKnowledgeTestById(Long id) {
        return knowledgeTestRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge test with id = "+id+" not found"));
    }

    // Add new knowledge test with request DTO
    public void addKnowledgeTest(KnowledgeTestDto dto) {
        if(knowledgeTestRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Knowledge test '" + dto.getName() + "' already exists");
        }

        KnowledgeTest knowledgeTest = new KnowledgeTest();
        knowledgeTest.setName(dto.getName());
        knowledgeTest.setCategoryName(dto.getCategory());
        knowledgeTest.setTestDate(dto.getTestDate());

        // Check if class with given id exists and set it to entity
        SchoolClass schoolClass = classRepository
                .findById(dto.getClassId())
                .orElseThrow(() -> new BadRequestException("Class with id=" + dto.getClassId() + " not found"));
        knowledgeTest.setSchoolClass(schoolClass);

        // Check if subject with given id exists and set it to entity
        Subject subject = subjectRepository
                .findById(dto.getSubjectId())
                .orElseThrow(() -> new BadRequestException("Subject with id=" + dto.getSubjectId() + " not found"));
        knowledgeTest.setSubject(subject);

        // Check if user with given id exists and set it to entity
        User teacher = userRepository
                .findById(dto.getTeacherId())
                .orElseThrow(() -> new BadRequestException("User with id=" + dto.getTeacherId() + " not found"));
        knowledgeTest.setTeacher(teacher);

        knowledgeTestRepository.save(knowledgeTest);
    }

    // Update attributes of knowledge test with given ID
    public void updateKnowledgeTestAttributes(KnowledgeTestUpdateDto dto, Long id) throws ResourceNotFoundException {
        // Find a knowledge test if exists in database
        Optional<KnowledgeTest> optionalTest = knowledgeTestRepository.findById(id);

        // If the knowledge test exists, update it
        if(optionalTest.isPresent()) {
            KnowledgeTest test = optionalTest.get();

            // Check if 'name' is given
            if(dto.getName() != null) {
                test.setName(dto.getName());
            }
            // Check if 'category' is given
            if(dto.getCategory() != null) {
                test.setCategoryName(dto.getCategory());
            }
            // Check if 'testDate' is given
            if(dto.getTestDate() != null) {
                test.setTestDate(dto.getTestDate());
            }
            // Check if 'classId' is given
            if(dto.getClassId() != null) {
                SchoolClass updatedClass = classRepository
                        .findById(dto.getClassId())
                        .orElseThrow(() -> new BadRequestException("School class with id=" + dto.getClassId() + " not found"));
                test.setSchoolClass(updatedClass);
            }
            if(dto.getSubjectId() != null) {
                Subject updatedSubject = subjectRepository
                        .findById(dto.getSubjectId())
                        .orElseThrow(() -> new BadRequestException("Subject with id=" + dto.getSubjectId() + " not found"));
                test.setSubject(updatedSubject);
            }
            if(dto.getTeacherId() != null) {
                User updatedTeacher = userRepository
                        .findById(dto.getTeacherId())
                        .orElseThrow(() -> new BadRequestException("User with id=" + dto.getTeacherId() + " not found"));
                test.setTeacher(updatedTeacher);
            }
            knowledgeTestRepository.save(test);
        } else {
            throw new ResourceNotFoundException("Knowledge test with id=" + id + " not found");
        }
    }

    // Delete a knowledge test with given ID
    public void deleteKnowledgeTest(Long id) {
        if(knowledgeTestRepository.existsById(id)) {
            knowledgeTestRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Knowledge test with id=" + id + " not found");
        }
    }
}

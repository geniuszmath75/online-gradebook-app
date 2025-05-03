package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.KnowledgeTestDto;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
}

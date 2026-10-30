-- Demo seed for local/dev use.
-- Passwords (BCrypt):
--   teachers: teacher123
--   students: student123
-- Unique inserts use ON CONFLICT DO NOTHING so leftover manual rows do not break migrate.

-- ========== Classes (2) ==========
INSERT INTO classes (name, created_at, updated_at)
VALUES ('1A', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('1B', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ========== Subjects (3) ==========
INSERT INTO subjects (name, created_at, updated_at)
VALUES ('matematyka', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('historia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('informatyka', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- ========== Teachers (3): 2 are class tutors (wychowawcy) ==========
-- Password hash for "teacher123"
INSERT INTO users (email, password, firstname, lastname, role, created_at, updated_at, class_id)
VALUES ('teacher.kowalski@example.com',
        '$2a$10$G0mcRKH2PUsF8jQWxAKZ1uXsyUuJUDn9gVHKR.QSpLSedftYz7Dq6',
        'Jan', 'Kowalski', 'TEACHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A')),
       ('teacher.nowak@example.com',
        '$2a$10$G0mcRKH2PUsF8jQWxAKZ1uXsyUuJUDn9gVHKR.QSpLSedftYz7Dq6',
        'Anna', 'Nowak', 'TEACHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B')),
       ('teacher.wisniewski@example.com',
        '$2a$10$G0mcRKH2PUsF8jQWxAKZ1uXsyUuJUDn9gVHKR.QSpLSedftYz7Dq6',
        'Piotr', 'Wiśniewski', 'TEACHER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        NULL)
ON CONFLICT (email) DO NOTHING;

INSERT INTO teachers_subjects (teacher_id, subject_id)
SELECT u.id, s.id
FROM (VALUES
          ('teacher.kowalski@example.com', 'matematyka'),
          ('teacher.nowak@example.com', 'historia'),
          ('teacher.wisniewski@example.com', 'informatyka')
     ) AS m(teacher_email, subject_name)
         JOIN users u ON u.email = m.teacher_email
         JOIN subjects s ON s.name = m.subject_name
WHERE NOT EXISTS (
    SELECT 1
    FROM teachers_subjects ts
    WHERE ts.teacher_id = u.id
      AND ts.subject_id = s.id
);

-- ========== Students (8): 4 per class ==========
-- Password hash for "student123"
INSERT INTO students (email, password, firstname, lastname, created_at, updated_at, class_id)
VALUES ('student.adamczyk@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Adam', 'Adamczyk', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A')),
       ('student.bialek@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Bartek', 'Białek', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A')),
       ('student.czubak@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Celina', 'Czubak', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A')),
       ('student.dabrowska@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Daria', 'Dąbrowska', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A')),
       ('student.ewiak@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Ewa', 'Ewiak', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B')),
       ('student.filipiak@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Filip', 'Filipiak', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B')),
       ('student.gorski@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Grzegorz', 'Górski', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B')),
       ('student.hajduk@example.com',
        '$2a$10$Vu9Mnr4JSrFm6qxafc3B..5sTecAYC9W1x2C6juUmFY772xhNPKh2',
        'Hanna', 'Hajduk', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B'))
ON CONFLICT (email) DO NOTHING;

-- ========== Knowledge tests (5), each a distinct category ==========
-- Class 1A: 3 tests (2 past/graded, 1 future)
-- Class 1B: 2 tests (1 past/graded, 1 future)
INSERT INTO knowledge_tests (name, category, test_date, created_at, updated_at, class_id, subject_id, teacher_id)
VALUES ('Równania liniowe',
        'CLASS_TEST',
        (CURRENT_DATE - INTERVAL '21 days')::date,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A'),
        (SELECT id FROM subjects WHERE name = 'matematyka'),
        (SELECT id FROM users WHERE email = 'teacher.kowalski@example.com')),
       ('Kartkówka – ułamki',
        'QUIZ',
        (CURRENT_DATE - INTERVAL '10 days')::date,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A'),
        (SELECT id FROM subjects WHERE name = 'matematyka'),
        (SELECT id FROM users WHERE email = 'teacher.kowalski@example.com')),
       ('Projekt – kalkulator',
        'HOMEWORK',
        (CURRENT_DATE + INTERVAL '14 days')::date,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1A'),
        (SELECT id FROM subjects WHERE name = 'matematyka'),
        (SELECT id FROM users WHERE email = 'teacher.kowalski@example.com')),
       ('Odpowiedź ustna – średniowiecze',
        'ORAL_ANSWER',
        (CURRENT_DATE - INTERVAL '7 days')::date,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B'),
        (SELECT id FROM subjects WHERE name = 'historia'),
        (SELECT id FROM users WHERE email = 'teacher.nowak@example.com')),
       ('Praca na lekcji – mapa Europy',
        'CLASSWORK',
        (CURRENT_DATE + INTERVAL '21 days')::date,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
        (SELECT id FROM classes WHERE name = '1B'),
        (SELECT id FROM subjects WHERE name = 'historia'),
        (SELECT id FROM users WHERE email = 'teacher.nowak@example.com'))
ON CONFLICT (name) DO NOTHING;

-- ========== Grades (12): 4 students × 3 graded tests ==========
INSERT INTO grades (grade, description, created_at, updated_at, student_id, test_id)
SELECT g.grade,
       g.description,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       s.id,
       t.id
FROM (VALUES
          ('student.adamczyk@example.com', 'Równania liniowe', 5.0::numeric, 'Bardzo dobra praca'),
          ('student.bialek@example.com', 'Równania liniowe', 4.0::numeric, 'Dobra robota'),
          ('student.czubak@example.com', 'Równania liniowe', 3.5::numeric, 'Jest postęp'),
          ('student.dabrowska@example.com', 'Równania liniowe', 5.0::numeric, 'Wzorowo'),
          ('student.adamczyk@example.com', 'Kartkówka – ułamki', 4.5::numeric, 'Solidnie'),
          ('student.bialek@example.com', 'Kartkówka – ułamki', 3.0::numeric, 'Wymaga powtórki'),
          ('student.czubak@example.com', 'Kartkówka – ułamki', 4.0::numeric, 'OK'),
          ('student.dabrowska@example.com', 'Kartkówka – ułamki', 5.0::numeric, 'Świetnie'),
          ('student.ewiak@example.com', 'Odpowiedź ustna – średniowiecze', 4.0::numeric, 'Dobra odpowiedź'),
          ('student.filipiak@example.com', 'Odpowiedź ustna – średniowiecze', 3.5::numeric, 'Niepełna'),
          ('student.gorski@example.com', 'Odpowiedź ustna – średniowiecze', 5.0::numeric, 'Doskonale'),
          ('student.hajduk@example.com', 'Odpowiedź ustna – średniowiecze', 4.5::numeric, 'Bardzo dobrze')
     ) AS g(student_email, test_name, grade, description)
         JOIN students s ON s.email = g.student_email
         JOIN knowledge_tests t ON t.name = g.test_name
WHERE NOT EXISTS (
    SELECT 1
    FROM grades existing
    WHERE existing.student_id = s.id
      AND existing.test_id = t.id
);

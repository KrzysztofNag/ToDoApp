-- V2__seed_test_data.sql


-- USERS
insert into users (email, password_hash, role, enabled)
values
    ('user@test.com', '$2a$12$Gpo56i3T511UIQr/V3VY6.L8bXkvFH.E7OH0yU0XDECBGNbiZW9BK', 'USER', true),
    ('admin@test.com', '$2a$12$TD4vMv.0nV1/1Rd7wjMtAO.3ERl1IFq1LDEgfk6JZTXFXh2BXCH5e', 'ADMIN', true);


-- TASKS dla user@test.com
insert into tasks (task_title, status, created_date, urgency, importance, user_id)
select
    t.task_title,
    t.status,
    now(),
    t.urgency,
    t.importance,
    u.id
from users u
         cross join (values
                         ('Kupić mleko', 'TODO', 'NOT_URGENT', 'NOT_IMPORTANT'),
                         ('Umyć naczynia', 'IN_PROGRESS', 'URGENT', 'IMPORTANT'),
                         ('Posprzątać pokój', 'TODO', 'NOT_URGENT', 'IMPORTANT'),
                         ('Zrobić trening', 'DONE', 'NOT_URGENT', 'IMPORTANT')
) as t(task_title, status, urgency, importance)
where u.email = 'user@test.com';


-- TASKS dla admin@test.com
insert into tasks (task_title, status, created_date, urgency, importance, user_id)
select
    t.task_title,
    t.status,
    now(),
    t.urgency,
    t.importance,
    u.id
from users u
         cross join (values
                         ('Opłacić rachunki', 'TODO', 'URGENT', 'IMPORTANT'),
                         ('Wyrzucić śmieci', 'TODO', 'NOT_URGENT', 'IMPORTANT')
) as t(task_title, status, urgency, importance)
where u.email = 'admin@test.com';
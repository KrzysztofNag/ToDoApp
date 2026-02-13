-- V1__init.sql

create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    role varchar(50) not null,
    enabled boolean not null default true
);

create table tasks (
    id bigserial primary key,
    task_title varchar(50) not null,
    status varchar(50) not null,
    created_date timestamp not null,
    urgency varchar(50) not null,
    importance varchar(50) not null,
    user_id bigint not null,

        constraint fk_tasks_user
            foreign key (user_id) references users(id)
                on delete cascade
);

create index idx_tasks_user_id on tasks(user_id);
create index idx_tasks_status on tasks(status);
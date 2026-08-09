create table if not exists "user"(
user_id serial primary key ,
username varchar(100) not null,
email varchar(150) not null unique,
password_hash varchar(255) not null,
reset_token varchar(255),
reset_token_expiry timestamp default now(),
last_login timestamp default now(),
status varchar(50)
);

create table if not exists role(
role_id serial primary key ,
role_name varchar(100) not null
);

create table if not exists user_role(
user_id int,
role_id int,
primary key (user_id, role_id),
foreign key (user_id) references "user"(user_id),
foreign key (role_id) references role(role_id)
);

create table if not exists audit_log(
log_id serial primary key ,
user_id int,
action varchar(255),
timestamp timestamp,
foreign key (user_id) references "user"(user_id)
);

create table if not exists subject(
subject_id serial primary key ,
create_time timestamp default now(),
last_update timestamp default now(),
subject_name varchar(150) not null
);

create table if not exists unit(
unit_id serial primary key ,
subject_id int not null,
unit_name varchar(150) not null,
create_time timestamp default now(),
last_update timestamp default now(),
foreign key (subject_id) references subject(subject_id)
);

create table if not exists topic(
topic_id serial primary key ,
unit_id int not null,
topic_name varchar(150) not null,
create_time timestamp default now(),
last_update timestamp default now(),
foreign key (unit_id) references unit(unit_id)
);

create table if not exists question(
question_id serial primary key ,
question text,
topic_id int not null,
author_id int not null,
mod_id int,
question_type varchar(50),
BTL_level varchar(50),
default_marks float,
status varchar(50),
create_time timestamp default now(),
last_update timestamp default now(),
foreign key (topic_id) references topic(topic_id),
foreign key (author_id) references "user"(user_id),
foreign key (mod_id) references "user"(user_id)
);

create table if not exists question_option(
option_id serial primary key ,
question_id int not null,
option_text text not null,
is_correct boolean not null default false,
create_time timestamp default now(),
last_update timestamp default now(),
foreign key (question_id) references question(question_id)
);

create table if not exists quiz(
quiz_id serial primary key ,
creator_id int not null,
subject_id int not null,
is_randomized boolean default false,
duration_minutes int,
status varchar(50),
create_time timestamp default now(),
last_update timestamp default now(),
foreign key (creator_id) references "user"(user_id),
foreign key (subject_id) references subject(subject_id)
);

create table if not exists quiz_question(
quiz_id int ,
question_id int,
marks_assigned float,
question_order int,
primary key (quiz_id, question_id),
foreign key (quiz_id) references quiz(quiz_id),
foreign key (question_id) references question(question_id),
unique (quiz_id,question_order)
);

create table if not exists quiz_attempt(
attempt_id serial primary key ,
quiz_id int not null,
student_id int not null,
start_time timestamp default now(),
last_saved_time timestamp default now(),
submit_time timestamp default now(),
score_obtained float,
status varchar(50),
create_time timestamp default now(),
last_update timestamp default now(),
foreign key (quiz_id) references quiz(quiz_id),
foreign key (student_id) references "user"(user_id)
);

create table if not exists student_answer(
answer_id serial primary key ,
attempt_id int unique not null,
question_id int not null,
selected_option_id int,
text_response text,
marks_awarded float,
is_auto_graded boolean default false,
evaluator_id int,
create_time timestamp default now(), 
last_update timestamp default now(),
foreign key (attempt_id) references quiz_attempt(attempt_id),
foreign key (question_id) references question(question_id),
foreign key (selected_option_id) references question_option(option_id),
foreign key (evaluator_id) references "user"(user_id),
unique (attempt_id,question_id)
);
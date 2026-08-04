create table "user"(
user_id int primary key ,
username varchar(100) not null,
email varchar(150) not null unique,
password_hash varchar(255) not null,
reset_token varchar(255),
reset_token_expiry timestamp,
last_login timestamp,
status varchar(50)
);

create table role(
role_id int primary key ,
role_name varchar(100) not null
);

create table user_role(
user_id int,
role_id int,
primary key (user_id, role_id),
foreign key (user_id) references "user"(user_id),
foreign key (role_id) references role(role_id)
);

create table audit_log(
log_id int primary key ,
user_id int,
action varchar(255),
timestamp timestamp,
foreign key (user_id) references "user"(user_id)
);

create table subject(
subject_id int primary key ,
subject_name varchar(150) not null
);

create table unit(
unit_id int primary key ,
subject_id int not null,
unit_name varchar(150) not null,
foreign key (subject_id) references subject(subject_id)
);

create table topic(
topic_id int primary key ,
unit_id int not null,
topic_name varchar(150) not null,
foreign key (unit_id) references unit(unit_id)
);

create table question(
question_id int primary key ,
question text,
topic_id int not null,
author_id int not null,
mod_id int,
question_type varchar(50),
BTL_level varchar(50),
default_marks float,
status varchar(50),
foreign key (topic_id) references topic(topic_id),
foreign key (author_id) references "user"(user_id),
foreign key (mod_id) references "user"(user_id)
);

create table question_option(
option_id int primary key ,
question_id int not null,
option_text text not null,
is_correct boolean not null default false,
foreign key (question_id) references question(question_id)
);

create table quiz(
quiz_id int primary key ,
creator_id int not null,
subject_id int not null,
is_randomized boolean default false,
duration_minutes int,
status varchar(50),
foreign key (creator_id) references "user"(user_id),
foreign key (subject_id) references subject(subject_id)
);

create table quiz_question(
quiz_id int,
question_id int,
marks_assigned float,
question_order int,
primary key (quiz_id, question_id),
foreign key (quiz_id) references quiz(quiz_id),
foreign key (question_id) references question(question_id)
);

create table quiz_attempt(
attempt_id int primary key ,
quiz_id int not null,
student_id int not null,
start_time timestamp,
last_saved_time timestamp,
submit_time timestamp,
score_obtained float,
status varchar(50),
foreign key (quiz_id) references quiz(quiz_id),
foreign key (student_id) references "user"(user_id)
);

create table student_answer(
answer_id int primary key ,
attempt_id int not null,
question_id int not null,
selected_option_id int,
text_response text,
marks_awarded float,
is_auto_graded boolean default false,
evaluator_id int,
foreign key (attempt_id) references quiz_attempt(attempt_id),
foreign key (question_id) references question(question_id),
foreign key (selected_option_id) references question_option(option_id),
foreign key (evaluator_id) references "user"(user_id)
);
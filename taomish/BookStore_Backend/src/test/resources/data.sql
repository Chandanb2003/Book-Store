-- Inserting 1 default ROLE_ADMIN user for testing, password = adminpassword --
insert into person (id, first_name, last_name, date_of_birth, email, password, role, registered_at) values (10000001, 'Admin', 'Admin', '2000-01-01', 'admin@email.com', '$2a$10$rW7AN0KxM4scb/65mKRLsulYZcMNOn3HynGSjSPjLA.9D5eQuQYk.', 'ROLE_ADMIN', '2023-11-23 13:23:05.262') on conflict do nothing;

-- Inserting 3 default ROLE_USER users for testing, password = userPassword --
insert into person (id, first_name, last_name, date_of_birth, email, password, role, registered_at) values (10000002, 'First Name 1', 'Last Name 1', '2000-01-01', 'email1@email.com', '$2a$10$sVk6EYpNZa8WELP4WbLT.OfLtR.WOuZkMP3eP1g.EOFbCZbQ5ZeeG', 'ROLE_USER', '2023-11-23 13:23:05.262') on conflict do nothing;
insert into person (id, first_name, last_name, date_of_birth, email, password, role, registered_at) values (10000003, 'First Name 2', 'Last Name 2', '2000-01-01', 'email2@email.com', '$2a$10$sVk6EYpNZa8WELP4WbLT.OfLtR.WOuZkMP3eP1g.EOFbCZbQ5ZeeG', 'ROLE_USER', '2023-11-23 13:23:05.262') on conflict do nothing;
insert into person (id, first_name, last_name, date_of_birth, email, password, role, registered_at) values (10000004, 'First Name 3', 'Last Name 3', '2000-01-01', 'email3@email.com', '$2a$10$sVk6EYpNZa8WELP4WbLT.OfLtR.WOuZkMP3eP1g.EOFbCZbQ5ZeeG', 'ROLE_USER', '2023-11-23 13:23:05.262') on conflict do nothing;

-- Inserting a few genres to initialize the default test book stock --
insert into genre (id, description) values (1, 'Genre 1') on conflict do nothing;
insert into genre (id, description) values (2, 'Genre 2') on conflict do nothing;

-- Inserting a few default books to have some stock available for testing --
insert into book (id, title, author, copies, copies_available, description, img) values (10000001, 'Title 1', 'Author 1', 10, 10, 'Description 1', 'Encoded image 1') on conflict do nothing;
insert into book (id, title, author, copies, copies_available, description, img) values (10000002, 'Title 2', 'Author 2', 10, 10, 'Description 2', 'Encoded image 2') on conflict do nothing;
insert into book (id, title, author, copies, copies_available, description, img) values (10000003, 'Title 3', 'Author 3', 10, 10, 'Description 3', 'Encoded image 3') on conflict do nothing;
insert into book (id, title, author, copies, copies_available, description, img) values (10000004, 'Title 4', 'Author 4', 10, 10, 'Description 4', 'Encoded image 4') on conflict do nothing;
insert into book (id, title, author, copies, copies_available, description, img) values (10000005, 'Title 5', 'Author 5', 10, 0,  'Description 5', 'Encoded image 5') on conflict do nothing;

-- Assigning genres to the books inserted above --
insert into book_genre (book_id, genre_id) values (10000001, 1) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000001, 2) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000002, 1) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000002, 2) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000003, 1) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000003, 2) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000004, 1) on conflict do nothing;
insert into book_genre (book_id, genre_id) values (10000005, 1) on conflict do nothing;

-- Assigning reviews to the books inserted above --
insert into review (id, person_email, person_first_name, person_last_name, book_id, date, rating, review_description) values (10000001, 'email1@email.com', 'First Name 1', 'Last Name 1', 10000001, '2024-01-01', 4.5, 'Description 1') on conflict do nothing;
insert into review (id, person_email, person_first_name, person_last_name, book_id, date, rating, review_description) values (10000002, 'email2@email.com', 'First Name 2', 'Last Name 2', 10000001, '2024-01-01', 3.5, 'Description 2') on conflict do nothing;

-- Inserting test payment fee for the first user --
insert into payment (id, person_email, amount) values (10000001, 'email1@email.com', 10.00) on conflict do nothing;

-- Inserting test history record for the first user --
insert into history_record (id, person_email, book_id, checkout_date, return_date) values (10000001, 'email1@email.com', 10000001, current_date - 10, current_date -5) on conflict do nothing;

-- Inserting test discussions for the first user --
insert into discussion (id, person_email, title, question, admin_email, response, closed) values (10000001, 'email1@email.com', 'Title 1', 'Question 1', null, null, false) on conflict do nothing;
insert into discussion (id, person_email, title, question, admin_email, response, closed) values (10000002, 'email1@email.com', 'Title 2', 'Question 2', 'admin@email.com', 'Response 1', true) on conflict do nothing;

-- Inserting test checkouts for different users --
insert into checkout (id, person_email, book_id, checkout_date, return_date) values (10000001, 'email1@email.com', 10000002, current_date, current_date + 7) on conflict do nothing;
insert into checkout (id, person_email, book_id, checkout_date, return_date) values (10000002, 'email1@email.com', 10000003, current_date, current_date + 7) on conflict do nothing;
insert into checkout (id, person_email, book_id, checkout_date, return_date) values (10000003, 'email2@email.com', 10000004, current_date - 3, current_date + 4) on conflict do nothing;
insert into checkout (id, person_email, book_id, checkout_date, return_date) values (10000004, 'email3@email.com', 10000004, current_date - 8, current_date - 1) on conflict do nothing;
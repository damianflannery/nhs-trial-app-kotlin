-- Demo seed data for local development
-- Run manually: psql $DATABASE_URL -f src/main/resources/db/seed-demo-data.sql
--
-- All NHS numbers are synthetic 10-digit test values that do NOT identify real people.
-- Blood-pressure values and side-effect notes are fictional.

BEGIN;

INSERT INTO person (nhs_number, first_name, last_name, email, dob, gender, created_at) VALUES
  ('9434765919', 'Alice',   'Smith',    'alice.smith@example.com',    '1985-03-12', 'Female', NOW() - INTERVAL '30 days'),
  ('1112223339', 'Bob',     'Jones',    'bob.jones@example.com',      '1972-07-04', 'Male',   NOW() - INTERVAL '29 days'),
  ('1234567890', 'Carol',   'Williams', 'carol.w@example.com',        '1990-11-22', 'Female', NOW() - INTERVAL '28 days'),
  ('0000000001', 'David',   'Brown',    'david.brown@example.com',    '1965-05-30', 'Male',   NOW() - INTERVAL '27 days'),
  ('0000000002', 'Emma',    'Taylor',   'emma.taylor@example.com',    '1978-09-08', 'Female', NOW() - INTERVAL '26 days'),
  ('0000000003', 'Frank',   'Anderson', 'frank.a@example.com',        '1955-01-17', 'Male',   NOW() - INTERVAL '25 days'),
  ('0000000004', 'Grace',   'Thomas',   'grace.t@example.com',        '1993-06-25', 'Female', NOW() - INTERVAL '24 days'),
  ('0000000005', 'Henry',   'Jackson',  'henry.j@example.com',        '1948-12-03', 'Male',   NOW() - INTERVAL '23 days'),
  ('0000000006', 'Isla',    'White',    'isla.white@example.com',     '1982-04-14', 'Female', NOW() - INTERVAL '22 days'),
  ('0000000007', 'James',   'Harris',   'james.harris@example.com',   '1970-08-19', 'Male',   NOW() - INTERVAL '21 days'),
  ('0000000008', 'Karen',   'Martin',   'karen.m@example.com',        '1959-02-28', 'Female', NOW() - INTERVAL '20 days'),
  ('0000000009', 'Liam',    'Garcia',   'liam.garcia@example.com',    '2001-10-07', 'Male',   NOW() - INTERVAL '19 days'),
  ('0000000010', 'Mia',     'Martinez', 'mia.m@example.com',          '1996-03-31', 'Female', NOW() - INTERVAL '18 days'),
  ('0000000011', 'Noah',    'Robinson', 'noah.r@example.com',         '1944-07-22', 'Male',   NOW() - INTERVAL '17 days'),
  ('0000000012', 'Olivia',  'Clark',    'olivia.clark@example.com',   '1988-05-16', 'Female', NOW() - INTERVAL '16 days'),
  ('0000000013', 'Peter',   'Rodriguez','peter.r@example.com',        '1962-11-11', 'Male',   NOW() - INTERVAL '15 days'),
  ('0000000014', 'Quinn',   'Lewis',    'quinn.l@example.com',        '1975-09-02', 'Female', NOW() - INTERVAL '14 days'),
  ('0000000015', 'Ryan',    'Lee',      'ryan.lee@example.com',       '1983-06-08', 'Male',   NOW() - INTERVAL '13 days'),
  ('0000000016', 'Sarah',   'Walker',   'sarah.w@example.com',        '1991-01-25', 'Female', NOW() - INTERVAL '12 days'),
  ('0000000017', 'Thomas',  'Hall',     'thomas.hall@example.com',    '1967-04-18', 'Male',   NOW() - INTERVAL '11 days');

INSERT INTO medical (person_id, bp_systolic, bp_diastolic, treatment, side_effects, created_at)
SELECT p.id, v.sys, v.dia, v.treatment, v.side_effects, NOW() - v.days_ago * INTERVAL '1 day'
FROM person p
JOIN (VALUES
  ('alice.smith@example.com',    118, 76,  'Drug',    NULL,                           30),
  ('bob.jones@example.com',      142, 91,  'Placebo', 'Mild headache',                29),
  ('carol.w@example.com',        125, 82,  'Drug',    NULL,                           28),
  ('david.brown@example.com',    155, 98,  'Placebo', 'Dizziness on standing',        27),
  ('emma.taylor@example.com',    119, 78,  'Drug',    'Slight nausea first week',     26),
  ('frank.a@example.com',        162, 104, 'Placebo', NULL,                           25),
  ('grace.t@example.com',        108, 68,  'Drug',    NULL,                           24),
  ('henry.j@example.com',        170, 108, 'Placebo', 'Fatigue',                      23),
  ('isla.white@example.com',     122, 80,  'Drug',    NULL,                           22),
  ('james.harris@example.com',   148, 94,  'Drug',    'Occasional dry cough',         21),
  ('karen.m@example.com',        135, 86,  'Placebo', NULL,                           20),
  ('liam.garcia@example.com',    112, 70,  'Drug',    NULL,                           19),
  ('mia.m@example.com',          127, 83,  'Placebo', 'Mild ankle swelling',          18),
  ('noah.r@example.com',         168, 106, 'Drug',    NULL,                           17),
  ('olivia.clark@example.com',   121, 79,  'Placebo', NULL,                           16),
  ('peter.r@example.com',        158, 100, 'Drug',    'Headache first two days',      15),
  ('quinn.l@example.com',        133, 85,  'Placebo', NULL,                           14),
  ('ryan.lee@example.com',       116, 74,  'Drug',    NULL,                           13),
  ('sarah.w@example.com',        129, 84,  'Placebo', 'Mild fatigue',                 12),
  ('thomas.hall@example.com',    145, 92,  'Drug',    NULL,                           11)
) AS v(email, sys, dia, treatment, side_effects, days_ago)
ON p.email = v.email;

COMMIT;

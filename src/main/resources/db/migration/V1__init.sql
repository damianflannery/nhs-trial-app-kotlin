CREATE TABLE person (
    id          SERIAL PRIMARY KEY,
    nhs_number  CHAR(10)       NOT NULL UNIQUE,
    first_name  VARCHAR(100)   NOT NULL,
    last_name   VARCHAR(100)   NOT NULL,
    email       VARCHAR(255)   NOT NULL UNIQUE,
    dob         DATE           NOT NULL,
    gender      VARCHAR(10)    NOT NULL,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE TABLE medical (
    id           SERIAL PRIMARY KEY,
    person_id    INT            NOT NULL REFERENCES person(id),
    bp_systolic  INT            NOT NULL,
    bp_diastolic INT            NOT NULL,
    treatment    VARCHAR(100)   NOT NULL,
    side_effects TEXT,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

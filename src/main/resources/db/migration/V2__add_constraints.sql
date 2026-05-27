-- Narrow column types to match valid data ranges
ALTER TABLE medical ALTER COLUMN bp_systolic  TYPE SMALLINT;
ALTER TABLE medical ALTER COLUMN bp_diastolic TYPE SMALLINT;
ALTER TABLE medical ALTER COLUMN treatment    TYPE VARCHAR(7);

-- Enforce valid gender values
ALTER TABLE person
    ADD CONSTRAINT person_gender_check
    CHECK (gender IN ('Male', 'Female'));

-- Enforce realistic blood-pressure ranges
ALTER TABLE medical
    ADD CONSTRAINT medical_bp_systolic_check
    CHECK (bp_systolic BETWEEN 50 AND 300);

ALTER TABLE medical
    ADD CONSTRAINT medical_bp_diastolic_check
    CHECK (bp_diastolic BETWEEN 50 AND 300);

-- Enforce valid treatment values
ALTER TABLE medical
    ADD CONSTRAINT medical_treatment_check
    CHECK (treatment IN ('Drug', 'Placebo'));

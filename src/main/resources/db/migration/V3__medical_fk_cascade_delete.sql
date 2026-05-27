-- Replace the plain FK on medical.person_id with one that cascades deletes,
-- so removing a person row automatically removes their medical record.
ALTER TABLE medical
    DROP CONSTRAINT medical_person_id_fkey;

ALTER TABLE medical
    ADD CONSTRAINT medical_person_id_fkey
    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE;

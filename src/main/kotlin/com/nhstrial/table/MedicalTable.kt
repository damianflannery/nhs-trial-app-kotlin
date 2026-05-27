package com.nhstrial.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object MedicalTable : Table("medical") {
    val id = integer("id").autoIncrement()
    val personId = integer("person_id").references(PersonTable.id)
    val bpSystolic = integer("bp_systolic")
    val bpDiastolic = integer("bp_diastolic")
    val treatment = varchar("treatment", 100)
    val sideEffects = text("side_effects").nullable()
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

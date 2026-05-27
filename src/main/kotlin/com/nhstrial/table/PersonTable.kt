package com.nhstrial.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime

object PersonTable : Table("person") {
    val id = integer("id").autoIncrement()
    val nhsNumber = char("nhs_number", 10)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val email = varchar("email", 255)
    val dob = date("dob")
    val gender = varchar("gender", 10)
    val createdAt = datetime("created_at")
    override val primaryKey = PrimaryKey(id)
}

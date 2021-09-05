package com.example.db.tables

import org.jetbrains.exposed.sql.Table

object UserTable : Table() {

    val email= varchar(name = "email",length = 512)
    val name= varchar(name = "name",length = 512)
    val hashPassword= varchar(name = "hashPassword",length = 512)


    override val primaryKey: PrimaryKey = PrimaryKey(email)
}
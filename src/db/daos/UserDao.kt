package com.example.db.daos

import com.example.db.tables.UserTable
import com.example.db.model.User
import com.example.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class UserDao {

    suspend fun insertUser(user: User) {
        dbQuery {
            UserTable.insert { ut ->
                ut[email] = user.email
                ut[hashPassword] = user.hashPwd
                ut[name] = user.name
            }
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return dbQuery {
            UserTable.select {
                UserTable.email.eq(email)
            }.map { mapRowToUser(it) }.singleOrNull()
        }
    }

    private fun mapRowToUser(row: ResultRow?): User?{
        return row?.let {
            User(it[UserTable.email],it[UserTable.hashPassword],it[UserTable.name])
        }
    }

}
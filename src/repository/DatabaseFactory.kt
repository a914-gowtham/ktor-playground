package com.example.repository

import com.example.db.tables.NoteTable
import com.example.db.tables.UserTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseFactory {

    fun init(){
      Database.connect(hikari())

      transaction {
          SchemaUtils.create(UserTable)
          SchemaUtils.create(NoteTable)
      }
    }

    fun hikari(): HikariDataSource {
        val config = HikariConfig()
        val uri= URI(System.getenv("DATABASE_URL"))
        val username= uri.userInfo.split(":").toTypedArray()[0]
        val password= uri.userInfo.split(":").toTypedArray()[1]
        config.apply {
            driverClassName = System.getenv("JDBC_DRIVER")
//            jdbcUrl = System.getenv("DATABASE_URL")
            jdbcUrl= "jdbc:postgresql://" + uri.host + ":" + uri.port + uri.path + "?sslmode=require" + "&user=$username&password=$password"
            maximumPoolSize = 4
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
          withContext(Dispatchers.IO){
              transaction { block() }
          }
}
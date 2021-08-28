package com.example

import com.example.repository.DatabaseFactory
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.gson.*
import io.ktor.features.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    DatabaseFactory.init()
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/notes"){
            val body= call.receive<String>()
            call.respond("All notes retrieved $body")
        }

        get("/note/{id}"){
            val id= call.parameters["id"]
            call.respond("Note retrieved $id by path parameter")
        }

        /*
     postgres=# CREATE USER tecmint WITH PASSWORD 'securep@wd';
postgres=# CREATE DATABASE tecmintdb;
postgres=# GRANT ALL PRIVILEGES ON DATABASE tecmintdb to tecmint;
postgres=# \q
     * */
        get("/note"){
            val id= call.request.queryParameters["id"]
            call.respond("Note retrieved $id by query parameter")
        }
      /*  post("/notes"){
            val body= call.receive<String>()
            call.respond("All notes inserted $body")
        }

        delete("/notes"){
            val body= call.receive<String>()
            call.respond("$body note deleted")
        }
*/
        route("/notes"){
            //localhost:8888/notes/create
            route("/create"){
                post{
                    val body= call.receive<String>()
                    call.respond("All notes created $body")
                }
            }
            post{
                val body= call.receive<String>()
                call.respond("$body note created")
            }
            delete{
                val body= call.receive<String>()
                call.respond("$body note deleted")
            }
        }

        put("/notes"){
            val body= call.receive<String>()
            call.respond("$body note put")
        }

        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

data class MySession(val count: Int = 0)


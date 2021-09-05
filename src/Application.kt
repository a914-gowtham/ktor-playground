package com.example

import com.example.auth.JwtService
import com.example.auth.hash
import com.example.db.daos.NoteDao
import com.example.db.daos.UserDao
import com.example.db.model.User
import com.example.repository.DatabaseFactory
import com.example.routes.NoteRoutes
import com.example.routes.UserRoutes
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.locations.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    DatabaseFactory.init()
    val userDao= UserDao()
    val noteDao= NoteDao()
    val jwtService= JwtService()
    val hashFunction= { s:String-> hash(s)}
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
        jwt("jwt"){
            verifier(jwtService.verifier)
            realm= "Note Server"
            validate {
                val payload=it.payload
                val email= payload.getClaim("email").asString()
                val user= userDao.getUserByEmail(email)
                user
            }
        }
    }

    install(Locations)

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("Hi ahammed!", contentType = ContentType.Text.Plain)
        }

        UserRoutes(userDao, jwtService, hashFunction)
        NoteRoutes(noteDao,hashFunction)
        get("/token") {
           val email= call.request.queryParameters["email"]
            val password= call.request.queryParameters["password"]
            val userName= call.request.queryParameters["username"]
            val user = User(email!!, hashFunction(password!!), userName!!)
            call.respond(jwtService.generateToken(user))
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


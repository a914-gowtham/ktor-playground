package com.example.routes

import com.example.db.daos.NoteDao
import com.example.db.model.Note
import com.example.db.model.SimpleResponse
import com.example.db.model.User
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val NOTES = "$API_VERSION/notes"
const val CREATE_NOTE = "$NOTES/create"
const val UPDATE_NOTE = "$NOTES/update"
const val DELETE_NOTE = "$NOTES/delete"

@Location(NOTES)
class NoteGetRoute

@Location(CREATE_NOTE)
class NoteCreateRoute

@Location(UPDATE_NOTE)
class NoteUpdateRoute

@Location(DELETE_NOTE)
class NoteDeleteRoute

fun Route.NoteRoutes(
    noteDao: NoteDao,
    hashFunction: (String) -> String
) {
    authenticate("jwt") {
        post<NoteCreateRoute> {
            val note = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, "Missing field exception ${e.message}")
                )
                return@post
            }
            try {
                val user = call.principal<User>()
                val email=user!!.email
                noteDao.addNote(note, email)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, "Note added!")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, "Missing field exception ${e.message}")
                )
            }
        }

        get<NoteGetRoute> {
            try {
                val email = call.principal<User>()!!.email
                val notes = noteDao.getAllNotes(email)
                call.respond(HttpStatusCode.OK, notes)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, emptyList<Note>())
            }
        }

        put<NoteUpdateRoute> {
            val note = try {
                call.receive<Note>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Missing Fields"))
                return@put
            }
            try {
                val email = call.principal<User>()!!.email
                noteDao.updateNote(note, email)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Note Updated Successfully!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem Occurred!"))
            }

        }

        delete<NoteDeleteRoute> {
            val noteId = try {
                call.request.queryParameters["id"]!!
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "QueryParameter:id is not present"))
                return@delete
            }
            try {
                val email = call.principal<User>()!!.email
                noteDao.deleteNote(noteId, email)
                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Note Deleted Successfully!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some problem Occurred!"))
            }
        }
    }
}
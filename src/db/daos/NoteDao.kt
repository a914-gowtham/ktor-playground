package com.example.db.daos

import com.example.db.model.Note
import com.example.db.tables.NoteTable
import com.example.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*

class NoteDao {

    suspend fun addNote(note: Note, email: String){
        dbQuery {
            NoteTable.insert { nt->
                nt[NoteTable.id] = note.id
                nt[NoteTable.userEmail] = email
                nt[NoteTable.noteTitle] = note.noteTitle
                nt[NoteTable.description] = note.description
                nt[NoteTable.date] = note.date
            }

        }

    }

    suspend fun getAllNotes(email:String):List<Note> = dbQuery {
        NoteTable.select {
            NoteTable.userEmail.eq(email)
        }.mapNotNull { rowToNote(it) }
    }

    suspend fun updateNote(note:Note,email: String){
        dbQuery {
            NoteTable.update(
                where = {
                    NoteTable.userEmail.eq(email) and NoteTable.id.eq(note.id)
                }
            ){ nt->
                nt[NoteTable.noteTitle] = note.noteTitle
                nt[NoteTable.description] = note.description
                nt[NoteTable.date] = note.date
            }
        }

    }

    suspend fun deleteNote(id:String,email: String){
        dbQuery {
            NoteTable.deleteWhere { NoteTable.userEmail.eq(email) and NoteTable.id.eq(id) }
        }
    }

    private fun rowToNote(row: ResultRow?): Note? {
        if(row == null){
            return null
        }
        return Note(
            id = row[NoteTable.id],
            noteTitle = row[NoteTable.noteTitle],
            description =  row[NoteTable.description],
            date = row[NoteTable.date]
        )

    }
}
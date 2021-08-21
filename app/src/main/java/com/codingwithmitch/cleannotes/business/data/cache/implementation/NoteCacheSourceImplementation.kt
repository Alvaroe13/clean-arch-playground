package com.codingwithmitch.cleannotes.business.data.cache.implementation

import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.domain.model.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteCacheSourceImplementation
@Inject
constructor(
    private val noteDao: NoteDaoService // TODO ( create class)
) : NoteCacheDataSource {
    override suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    override suspend fun deleteNote(primaryKey: String): Int = noteDao.deleteNote(primaryKey)

    override suspend fun deleteNotes(noteList: List<Note>): Int = noteDao.deleteNotes( noteList)

    override suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String): Int
    = noteDao.updateNote( primaryKey , newTitle , newBody)

    override suspend fun searcNotes(query: String, filterAndOrder: String, page: Int): List<Note>
    = noteDao.searcNotes( query , filterAndOrder , page)

    override suspend fun searchNoteById(primaryKey: String): Note?
    = noteDao.searchNoteById( primaryKey )

    override suspend fun getNumNotes(): Int = noteDao.getNumNotes()

    override suspend fun insertNotes(notes: List<Note>): LongArray = noteDao.updateNote(notes)
}
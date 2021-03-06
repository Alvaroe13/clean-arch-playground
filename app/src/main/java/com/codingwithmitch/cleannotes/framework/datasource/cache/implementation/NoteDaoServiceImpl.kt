package com.codingwithmitch.cleannotes.framework.datasource.cache.implementation

import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.util.DateUtil
import com.codingwithmitch.cleannotes.framework.datasource.cache.abstraction.NoteDaoService
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.NoteDao
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.returnOrderedQuery
import com.codingwithmitch.cleannotes.framework.datasource.cache.mappers.CacheMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDaoServiceImpl
@Inject
constructor(
    private val noteDao: NoteDao,
    private val noteMapper: CacheMapper,
    private val dateUtil: DateUtil
) : NoteDaoService {
    override suspend fun insertNote(note: Note): Long =
        noteDao.insertNote(noteMapper.mapEntityFromNote(note))


    override suspend fun insertNotes(notes: List<Note>): LongArray = noteDao.insertNotes(
        noteMapper.noteListToEntityList(notes)
    )


    override suspend fun searchNoteById(id: String): Note? =
        noteDao.searchNoteById(id)?.let { entity ->
            noteMapper.mapNoteFromEntity(entity)
        }


    override suspend fun updateNote(primaryKey: String, title: String, body: String?): Int =
        noteDao.updateNote(
            primaryKey = primaryKey,
            title = title,
            body = body,
            updated_at = dateUtil.getCurrentTimestamp()
        )


    override suspend fun deleteNote(primaryKey: String): Int = noteDao.deleteNote(primaryKey)


    override suspend fun deleteNotes(notes: List<Note>): Int {
        val ids = notes.mapIndexed { index, value -> value.id }
        return noteDao.deleteNotes(ids)
    }

    override suspend fun searchNotes(): List<Note> =
        noteMapper.entityListToNoteList(
            noteDao.searchNotes()
        )


    override suspend fun searchNotesOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> = noteMapper.entityListToNoteList(
        noteDao.searchNotesOrderByDateDESC(
            query = query,
            page = page,
            pageSize = pageSize
        )
    )


    override suspend fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> = noteMapper.entityListToNoteList(
        noteDao.searchNotesOrderByDateASC(
            query = query,
            page = page,
            pageSize = pageSize
        )
    )


    override suspend fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> = noteMapper.entityListToNoteList(
        noteDao.searchNotesOrderByTitleDESC(
            query = query,
            page = page,
            pageSize = pageSize
        )
    )


    override suspend fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Note> = noteMapper.entityListToNoteList(
        noteDao.searchNotesOrderByTitleASC(
            query = query,
            page = page,
            pageSize = pageSize
        )
    )


    override suspend fun getNumNotes(): Int = noteDao.getNumNotes()

    override suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note> = noteMapper.entityListToNoteList(
        noteDao.returnOrderedQuery(
            query = query,
            page = page,
            filterAndOrder = filterAndOrder
        )
    )


    override suspend fun getAllNotes(): List<Note> =
        noteMapper.entityListToNoteList(
            noteDao.searchNotes()
        )

}
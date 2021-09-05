package com.codingwithmitch.cleannotes.framework.datasource.cache.util

import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.business.domain.util.EntityMapper
import com.codingwithmitch.cleannotes.framework.datasource.cache.model.NoteCacheEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps Note to NoteCacheEntity or NoteCacheEntity to Note.
 */
@Singleton
class CacheMapper
@Inject
constructor() : EntityMapper<NoteCacheEntity, Note> {

    fun entityListToNoteList(entities: List<NoteCacheEntity>): List<Note>{
        val list: ArrayList<Note> = ArrayList()
        for(entity in entities){
            list.add( mapDomainModelFromEntity(entity) )
        }
        return list
    }

    fun noteListToEntityList(notes: List<Note>): List<NoteCacheEntity>{
        val entities: ArrayList<NoteCacheEntity> = ArrayList()
        for(note in notes){
            entities.add( mapEntityFromDomainModel(note) )
        }
        return entities
    }

    override fun mapDomainModelFromEntity(entity: NoteCacheEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            updated_at = entity.updated_at,
            created_at = entity.created_at
        )
    }

    override fun mapEntityFromDomainModel(domainModel: Note): NoteCacheEntity {
        return NoteCacheEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at
        )
    }
}
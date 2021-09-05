package com.codingwithmitch.cleannotes.business.domain.util

/**
 * WE've got Note model class and NoteEntity class, this fun are going to make easy to map
 * one from the other when needed.
 */
interface EntityMapper<Entity, DomainModel> {

    fun mapNoteFromEntity(entity: Entity): DomainModel
    fun mapEntityFromNote(domainModel: DomainModel): Entity
    
}
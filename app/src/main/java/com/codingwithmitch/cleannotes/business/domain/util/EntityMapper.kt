package com.codingwithmitch.cleannotes.business.domain.util

interface EntityMapper<Entity, DomainModel> {

    fun mapDomainModelFromEntity(entity: Entity): DomainModel
    fun mapEntityFromDomainModel(domainModel: DomainModel): Entity
    
}
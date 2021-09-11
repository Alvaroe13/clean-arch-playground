package com.codingwithmitch.cleannotes.framework.datasource.network.implementation

import com.codingwithmitch.cleannotes.business.domain.model.Note
import com.codingwithmitch.cleannotes.framework.datasource.network.abstraction.NoteFirestoreService
import com.codingwithmitch.cleannotes.framework.datasource.network.mappers.NetworkMapper
import com.codingwithmitch.cleannotes.framework.datasource.network.model.NoteNetworkEntity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Firestore doc refs:
 * 1. add:  https://firebase.google.com/docs/firestore/manage-data/add-data
 * 2. delete: https://firebase.google.com/docs/firestore/manage-data/delete-data
 * 3. update: https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
 * 4. query: https://firebase.google.com/docs/firestore/query-data/queries
 */
@Singleton
class NoteFirestoreServiceImpl
@Inject
constructor(
    private val fiebaseAuth: FirebaseAuth, //added just in case
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
) : NoteFirestoreService {

    override suspend fun insertOrUpdateNote(note: Note) {
        val noteEntity = networkMapper.mapEntityFromNote(note)
        noteEntity.updated_at = Timestamp.now()

        getNotesNode()
            .document(noteEntity.id)
            .set(noteEntity)
            .await()
    }

    override suspend fun insertOrUpdateNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot insert more than 500 notes at a time into firestore.")
        }

        val collectionRef = getNotesNode()

        firestore.runBatch { writeBatch ->
            notes.forEach {
                val entity = networkMapper.mapEntityFromNote(it)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(it.id)
                writeBatch.set(documentRef, entity)
            }
        }.await()
    }

    override suspend fun deleteNote(primaryKey: String) {
        getNotesNode()
            .document(primaryKey)
            .delete()
            .await()
    }


    override suspend fun insertDeletedNote(note: Note) {
        val noteEntity = networkMapper.mapEntityFromNote(note)
        getDeletesNode()
            .document(noteEntity.id)
            .set(noteEntity)
            .await()
    }

    override suspend fun insertDeletedNotes(notes: List<Note>) {
        if (notes.size > 500) {
            throw Exception("Cannot delete more than 500 notes at a time in firestore.")
        }

        val deletesCollectionRef = getDeletesNode()

        firestore.runBatch { writeBatch ->
            notes.forEach {
                val documentRef = deletesCollectionRef.document(it.id)
                writeBatch.set(documentRef, networkMapper.mapEntityFromNote(it))
            }
        }.await()

    }

    override suspend fun deleteDeletedNote(note: Note) {
        val entity = networkMapper.mapEntityFromNote(note)
        getDeletesNode()
            .document(entity.id)
            .delete()
            .await()
    }

    /** for testing only */
    override suspend fun deleteAllNotes() {
        firestore
            .collection(NOTES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
    }

    override suspend fun getDeletedNotes(): List<Note> =
        networkMapper.entityListToNoteList(
            getDeletesNode()
                .get()
                .await()
                .toObjects(NoteNetworkEntity::class.java)
        )


    override suspend fun searchNote(note: Note): Note? =
        getNotesNode()
            .document(note.id)
            .get()
            .await()
            .toObject(NoteNetworkEntity::class.java)?.let {
                networkMapper.mapNoteFromEntity(it)
            }


    override suspend fun getAllNotes(): List<Note> =
        networkMapper.entityListToNoteList(
            getNotesNode()
                .get()
                .await()
                .toObjects(NoteNetworkEntity::class.java)
        )


    private fun getNotesNode(): CollectionReference =
        firestore.collection(NOTES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)

    private fun getDeletesNode(): CollectionReference =
        firestore.collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(NOTES_COLLECTION)

    companion object {
        const val NOTES_COLLECTION = "notes"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID = "uTgS01Bx4uVtwMi6vasGE829TzN2" // hardcoded for single user
        const val EMAIL = "mitch@tabian.ca"
    }
}
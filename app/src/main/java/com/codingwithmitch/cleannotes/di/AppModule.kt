package com.codingwithmitch.cleannotes.di

import android.content.SharedPreferences
import com.codingwithmitch.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.codingwithmitch.cleannotes.business.data.cache.implementation.NoteCacheSourceImpl
import com.codingwithmitch.cleannotes.business.data.remote.abstraction.NoteRemoteDataSource
import com.codingwithmitch.cleannotes.business.data.remote.implementation.NoteRemoteSourceImpl
import com.codingwithmitch.cleannotes.business.domain.model.NoteFactory
import com.codingwithmitch.cleannotes.business.domain.util.DateUtil
import com.codingwithmitch.cleannotes.business.interactors.common.DeleteNote
import com.codingwithmitch.cleannotes.business.interactors.notedetail.NoteDetailInteractors
import com.codingwithmitch.cleannotes.business.interactors.notedetail.UpdateNote
import com.codingwithmitch.cleannotes.business.interactors.notelist.*
import com.codingwithmitch.cleannotes.business.interactors.splash.SyncDeletedNotes
import com.codingwithmitch.cleannotes.business.interactors.splash.SyncNotes
import com.codingwithmitch.cleannotes.framework.datasource.cache.abstraction.NoteDaoService
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.NoteDao
import com.codingwithmitch.cleannotes.framework.datasource.cache.database.NoteDatabase
import com.codingwithmitch.cleannotes.framework.datasource.cache.implementation.NoteDaoServiceImpl
import com.codingwithmitch.cleannotes.framework.datasource.cache.mappers.CacheMapper
import com.codingwithmitch.cleannotes.framework.datasource.network.abstraction.NoteFirestoreService
import com.codingwithmitch.cleannotes.framework.datasource.network.implementation.NoteFirestoreServiceImpl
import com.codingwithmitch.cleannotes.framework.datasource.network.mappers.NetworkMapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Module
object AppModule {


    @JvmStatic
    @Singleton
    @Provides
    fun providesDateFormat(): SimpleDateFormat {
        val DATE_FORMAT = "yyyy-MM-dd hh:mm:ss a"
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
        val TIME_ZONE = "UTC-7"
        sdf.timeZone = TimeZone.getTimeZone(TIME_ZONE)
        return sdf
    }


    @JvmStatic
    @Singleton
    @Provides
    fun provideDateUtil(dateFormat: SimpleDateFormat): DateUtil = DateUtil(dateFormat)


    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPrefsEditor(sharedPreferences: SharedPreferences): SharedPreferences.Editor =
        sharedPreferences.edit()


    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteFactory(dateUtil: DateUtil): NoteFactory = NoteFactory(dateUtil)

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDAO(noteDatabase: NoteDatabase): NoteDao = noteDatabase.noteDao()


    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheMapper(dateUtil: DateUtil): CacheMapper = CacheMapper()


    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkMapper(dateUtil: DateUtil): NetworkMapper = NetworkMapper(dateUtil)


    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDaoService(
        noteDao: NoteDao,
        noteEntityMapper: CacheMapper,
        dateUtil: DateUtil
    ): NoteDaoService = NoteDaoServiceImpl(noteDao, noteEntityMapper, dateUtil)

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteCacheDataSource(
        noteDaoService: NoteDaoService
    ): NoteCacheDataSource = NoteCacheSourceImpl(noteDaoService)


    @JvmStatic
    @Singleton
    @Provides
    fun provideFirestoreService(
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore,
        networkMapper: NetworkMapper
    ): NoteFirestoreService =  NoteFirestoreServiceImpl(
            firebaseAuth,
            firebaseFirestore,
            networkMapper
        )


    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteNetworkDataSource(
        firestoreService: NoteFirestoreServiceImpl
    ): NoteRemoteDataSource = NoteRemoteSourceImpl( firestoreService   )


    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncNotes(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteRemoteDataSource
    ): SyncNotes = SyncNotes( noteCacheDataSource, noteNetworkDataSource )


    @JvmStatic
    @Singleton
    @Provides
    fun provideSyncDeletedNotes(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteRemoteDataSource
    ): SyncDeletedNotes = SyncDeletedNotes( noteCacheDataSource, noteNetworkDataSource )



    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteDetailInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteRemoteDataSource
    ): NoteDetailInteractors {
        return NoteDetailInteractors(
            DeleteNote(noteCacheDataSource, noteNetworkDataSource),
            UpdateNote(noteCacheDataSource, noteNetworkDataSource)
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideNoteListInteractors(
        noteCacheDataSource: NoteCacheDataSource,
        noteNetworkDataSource: NoteRemoteDataSource,
        noteFactory: NoteFactory
    ): NoteListInteractors {
        return NoteListInteractors(
            InsertNewNote(noteCacheDataSource, noteNetworkDataSource, noteFactory),
            DeleteNote(noteCacheDataSource, noteNetworkDataSource),
            SearchNotes(noteCacheDataSource),
            GetNumNotes(noteCacheDataSource),
            RestoreDeletedNote(noteCacheDataSource, noteNetworkDataSource),
            DeleteMultipleNotes(noteCacheDataSource, noteNetworkDataSource),
            GetAllNotes(noteCacheDataSource)
        )
    }

}
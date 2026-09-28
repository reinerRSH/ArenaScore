package com.example.arena.Model.Di

import android.content.Context
import androidx.room.Room
import com.example.arena.Model.Di.Daos.UserDao
import com.example.arena.Model.Di.Data.ArenaDatabase
import com.example.arena.repository.FacilityRepository
import com.example.arena.repository.FacilityRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
         return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideArenaDatabase(@ApplicationContext context: Context): ArenaDatabase {
        return Room.databaseBuilder(
            context,
            ArenaDatabase::class.java,
            "arena_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: ArenaDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideFacilityRepository(): FacilityRepository {
        return FacilityRepositoryImpl()
    }
}

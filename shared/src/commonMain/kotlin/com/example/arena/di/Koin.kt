package com.example.arena.di

import com.example.arena.repository.FacilityRepository
import com.example.arena.repository.FacilityRepositoryImpl
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val sharedModule = module {
    single<FacilityRepository> { FacilityRepositoryImpl() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(sharedModule)
    }

// For iOS
fun initKoin() = initKoin {}
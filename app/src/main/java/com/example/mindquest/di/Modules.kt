package com.example.mindquest.di

import androidx.room.Room
import com.example.mindquest.data.local.MIGRATION_1_2
import com.example.mindquest.data.local.MindQuestDatabase
import com.example.mindquest.data.remote.TriviaApi
import com.example.mindquest.data.repository.QuizRepository
import com.example.mindquest.data.repository.QuizRepositoryImpl
import com.example.mindquest.data.repository.StatsRepository
import com.example.mindquest.data.repository.StatsRepositoryImpl
import com.example.mindquest.data.settings.SettingsRepository
import com.example.mindquest.data.settings.SettingsRepositoryImpl
import com.example.mindquest.data.settings.settingsDataStore
import com.example.mindquest.ui.activityscreen.memory.MemoryMatchViewModel
import com.example.mindquest.ui.activityscreen.quiz.QuizViewModel
import com.example.mindquest.ui.landing.LandingViewModel
import com.example.mindquest.ui.settings.SettingsViewModel
import com.example.mindquest.ui.stats.StatisticsViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single { Dispatchers.IO }
    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(TriviaApi::class.java) }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), MindQuestDatabase::class.java, "mindquest.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { get<MindQuestDatabase>().quizQuestionDao() }
    single { get<MindQuestDatabase>().activityResultDao() }
    single { androidContext().settingsDataStore }
}

val repositoryModule = module {
    single<QuizRepository> { QuizRepositoryImpl(get(), get(), get()) }
    single<StatsRepository> { StatsRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { LandingViewModel(get()) }
    viewModel { QuizViewModel(get(), get(), get()) }
    viewModel { MemoryMatchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { StatisticsViewModel(get()) }
}

val appModules = listOf(networkModule, databaseModule, repositoryModule, viewModelModule)

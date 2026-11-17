package com.example.infocruce

import android.app.Application
import com.example.infocruce.data.repository.InMemoryHorseRepository

class InfoCruzaApplication : Application() {

    lateinit var repository: InMemoryHorseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = InMemoryHorseRepository()
    }
}

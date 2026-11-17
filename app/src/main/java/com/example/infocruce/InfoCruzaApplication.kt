package com.example.infocruce

import android.app.Application
import com.example.infocruce.data.repository.HorseRepository
import com.example.infocruce.data.repository.InMemoryHorseRepository
import com.example.infocruce.data.session.AppSession

class InfoCruzaApplication : Application() {

    lateinit var repository: HorseRepository
        private set

    lateinit var session: AppSession
        private set

    override fun onCreate() {
        super.onCreate()
        repository = InMemoryHorseRepository()
        session = AppSession(repository)
    }
}

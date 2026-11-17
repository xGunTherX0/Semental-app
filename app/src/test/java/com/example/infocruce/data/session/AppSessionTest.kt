package com.example.infocruce.data.session

import com.example.infocruce.data.repository.InMemoryHorseRepository
import com.example.infocruce.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests del login simulado (selector de perfil).
 */
class AppSessionTest {

    private val repository = InMemoryHorseRepository()
    private val session = AppSession(repository)

    @Test
    fun `la sesion arranca como dueno`() {
        assertEquals(UserRole.OWNER, session.currentUser.value.role)
        assertEquals(UserRole.OWNER, session.currentRole)
    }

    @Test
    fun `cambiar a veterinario cambia el usuario activo`() {
        session.selectRole(UserRole.VET)

        assertEquals(UserRole.VET, session.currentUser.value.role)
        assertEquals("Dra. Elena Vidal", session.currentUser.value.name)
    }

    @Test
    fun `cambiar a administrador cambia el usuario activo`() {
        session.selectRole(UserRole.ADMIN)

        assertEquals(UserRole.ADMIN, session.currentUser.value.role)
        assertEquals("Ing. Pablo León", session.currentUser.value.name)
    }

    @Test
    fun `cada rol expone su usuario de referencia`() {
        assertEquals(UserRole.OWNER, repository.getUserForRole(UserRole.OWNER).role)
        assertEquals(UserRole.VET, repository.getUserForRole(UserRole.VET).role)
        assertEquals(UserRole.ADMIN, repository.getUserForRole(UserRole.ADMIN).role)
    }
}

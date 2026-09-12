package com.lingubible.app.data.repository

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.Session
import com.lingubible.app.domain.model.User
import com.lingubible.app.domain.repository.AuthRepository
import com.lingubible.app.domain.util.EmailValidator
import io.appwrite.ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppwriteAuthRepository(
    private val clientProvider: AppwriteClientProvider
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override fun isValidEmail(email: String): Boolean {
        return EmailValidator.isValidLingnanEmail(email)
    }

    override suspend fun login(email: String, password: String): Result<Session> {
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Please use a valid Lingnan University email (@ln.hk or @ln.edu.hk)"))
        }

        val account = clientProvider.account 
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            val appwriteSession = account.createEmailPasswordSession(email.trim(), password)
            val appwriteUser = account.get()
            
            _currentUser.value = User(
                id = appwriteUser.id,
                name = appwriteUser.name,
                email = appwriteUser.email,
                emailVerification = appwriteUser.emailVerification,
                status = appwriteUser.status,
                registrationDate = appwriteUser.registration
            )

            Result.success(
                Session(
                    id = appwriteSession.id,
                    userId = appwriteSession.userId,
                    expire = appwriteSession.expire,
                    provider = appwriteSession.provider
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Please use a valid Lingnan University email (@ln.hk or @ln.edu.hk)"))
        }

        val account = clientProvider.account 
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            val appwriteUser = account.create(
                userId = ID.unique(),
                email = email.trim(),
                password = password,
                name = name.trim()
            )

            val user = User(
                id = appwriteUser.id,
                name = appwriteUser.name,
                email = appwriteUser.email,
                emailVerification = appwriteUser.emailVerification,
                status = appwriteUser.status,
                registrationDate = appwriteUser.registration
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        val account = clientProvider.account 
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            account.deleteSession("current")
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            _currentUser.value = null
            Result.failure(e)
        }
    }

    override suspend fun checkSession(): Result<User?> {
        val account = clientProvider.account ?: return Result.success(null)

        return try {
            val appwriteUser = account.get()
            val user = User(
                id = appwriteUser.id,
                name = appwriteUser.name,
                email = appwriteUser.email,
                emailVerification = appwriteUser.emailVerification,
                status = appwriteUser.status,
                registrationDate = appwriteUser.registration
            )
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            _currentUser.value = null
            Result.success(null)
        }
    }
}

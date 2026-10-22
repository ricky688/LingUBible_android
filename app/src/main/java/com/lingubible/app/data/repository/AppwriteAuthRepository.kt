package com.lingubible.app.data.repository

import androidx.activity.ComponentActivity
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.Session
import com.lingubible.app.domain.model.User
import com.lingubible.app.domain.repository.AuthRepository
import com.lingubible.app.domain.util.EmailValidator
import io.appwrite.ID
import io.appwrite.enums.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppwriteAuthRepository(
    private val clientProvider: AppwriteClientProvider
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isInitialSessionChecked = MutableStateFlow(false)
    override val isInitialSessionChecked: StateFlow<Boolean> = _isInitialSessionChecked.asStateFlow()

    private val _googleLinkedState = MutableStateFlow(false)
    override val googleLinkedState: StateFlow<Boolean> = _googleLinkedState.asStateFlow()

    private val sessionCheckMutex = Mutex()

    override fun isValidEmail(email: String): Boolean {
        return EmailValidator.isValidStudentEmail(email)
    }

    override suspend fun login(email: String, password: String): Result<Session> {
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Please use a valid Lingnan University email (@ln.hk)"))
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
            _isInitialSessionChecked.value = true

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
            return Result.failure(IllegalArgumentException("Please use a valid Lingnan University email (@ln.hk)"))
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

    override suspend fun loginWithGoogle(activity: ComponentActivity): Result<User> {
        val account = clientProvider.account
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            account.createOAuth2Session(activity, OAuthProvider.GOOGLE)
            val appwriteUser = account.get()
            if (!isValidEmail(appwriteUser.email)) {
                account.deleteSession("current")
                return Result.failure(
                    IllegalArgumentException("Please use a valid Lingnan University Google account (@ln.hk)")
                )
            }

            val user = User(
                id = appwriteUser.id,
                name = appwriteUser.name,
                email = appwriteUser.email,
                emailVerification = appwriteUser.emailVerification,
                status = appwriteUser.status,
                registrationDate = appwriteUser.registration
            )
            _currentUser.value = user
            _googleLinkedState.value = true
            _isInitialSessionChecked.value = true
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
            _googleLinkedState.value = false
            _isInitialSessionChecked.value = true
            Result.success(Unit)
        } catch (e: Exception) {
            _currentUser.value = null
            _googleLinkedState.value = false
            _isInitialSessionChecked.value = true
            Result.failure(e)
        }
    }

    override suspend fun checkSession(): Result<User?> = sessionCheckMutex.withLock {
        val account = clientProvider.account
        if (account == null) {
            _isInitialSessionChecked.value = true
            return@withLock Result.success(null)
        }

        return@withLock try {
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
            try {
                val identities = account.listIdentities()
                _googleLinkedState.value = identities.identities.any { it.provider.equals("google", ignoreCase = true) }
            } catch (_: Exception) {
                // Keep existing state if identity check fails
            }
            Result.success(user)
        } catch (e: Exception) {
            _currentUser.value = null
            _googleLinkedState.value = false
            Result.success(null)
        } finally {
            _isInitialSessionChecked.value = true
        }
    }

    override suspend fun updateName(name: String): Result<User> {
        val account = clientProvider.account 
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            val appwriteUser = account.updateName(name.trim())
            val updatedUser = User(
                id = appwriteUser.id,
                name = appwriteUser.name,
                email = appwriteUser.email,
                emailVerification = appwriteUser.emailVerification,
                status = appwriteUser.status,
                registrationDate = appwriteUser.registration
            )
            _currentUser.value = updatedUser
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePassword(newPassword: String, oldPassword: String): Result<Unit> {
        val account = clientProvider.account 
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            account.updatePassword(password = newPassword, oldPassword = oldPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isGoogleLinked(): Result<Boolean> {
        val account = clientProvider.account 
            ?: return Result.success(false)

        return try {
            val identities = account.listIdentities()
            val linked = identities.identities.any { it.provider.equals("google", ignoreCase = true) }
            _googleLinkedState.value = linked
            Result.success(linked)
        } catch (e: Exception) {
            Result.success(_googleLinkedState.value)
        }
    }

    override suspend fun unlinkGoogle(): Result<Unit> {
        val account = clientProvider.account 
            ?: return Result.failure(IllegalStateException("Appwrite Account service not initialized"))

        return try {
            val identities = account.listIdentities()
            val googleIdentity = identities.identities.firstOrNull { it.provider.equals("google", ignoreCase = true) }
                ?: return Result.failure(IllegalStateException("No Google account linked"))

            account.deleteIdentity(googleIdentity.id)
            _googleLinkedState.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


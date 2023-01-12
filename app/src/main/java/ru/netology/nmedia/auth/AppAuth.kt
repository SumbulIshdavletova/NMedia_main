package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppAuth private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state: MutableStateFlow<AuthState?>

    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"

        private var INSTANCE: AppAuth? = null

        fun getInstance(): AppAuth = requireNotNull(INSTANCE) {
            "init() must called before getInstance()"
        }

        fun init (context: Context) {
            INSTANCE = AppAuth(context)
        }
    }

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)

        _state = if (token == null || !prefs.contains(ID_KEY)) {
            prefs.edit { clear() }
            MutableStateFlow(null)
        } else {
            MutableStateFlow(AuthState(id, token))
        }
    }
    val state = _state.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String) { //передать сюда данные с viewModel для отдельного фрагмента авторизации
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        _state.value = AuthState(id, token)
    }

    @Synchronized
    fun removeAuth() {
        prefs.edit { clear() }
        _state.value = null
    }
}
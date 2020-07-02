package com.hapi.aoptest.ui.login

import android.telephony.gsm.GsmCellLocation
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.hapi.aoptest.data.LoginRepository
import com.hapi.aoptest.data.Result

import com.hapi.aoptest.R
import kotlinx.coroutines.*

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun checkLogin() {
        launch({
            if (!isUserNameValid("")) {
                _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
            } else if (!isPasswordValid("")) {
                _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
            } else {
                _loginForm.value = LoginFormState(isDataValid = true)
            }
        },{
            it.printStackTrace()

        })
    }


    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }


    /**
     * 所有网络请求都在 viewModelScope 域中启动，当页面销毁时会自动
     * 调用ViewModel的  #onCleared 方法取消所有协程
     */
    fun launch(
        block: suspend CoroutineScope.() -> Unit,
        error: suspend CoroutineScope.(Throwable) -> Unit = {},
        complete: suspend CoroutineScope.() -> Unit = {}
    ) {
        GlobalScope.launch {
            handleLaunch(
                withContext(Dispatchers.IO) { block },
                { error(it) },
                {
                    complete()
                }
            )
        }

    }



/**
 * 启动统一处理
 */
private suspend fun handleLaunch(
    block: suspend CoroutineScope.() -> Unit,
    error: suspend CoroutineScope.(Throwable) -> Unit,
    complete: suspend CoroutineScope.() -> Unit
) {
    coroutineScope {
        try {
            block()
        } catch (e: Throwable) {
            error(e)
        } finally {
            complete()
        }
    }
}

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        GlobalScope.launch {
            Log.d("sss","ss")
        }

        return password.length > 5
    }
}

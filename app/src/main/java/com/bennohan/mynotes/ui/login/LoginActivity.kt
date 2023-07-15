package com.bennohan.mynotes.ui.login

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.databinding.ActivityLoginBinding
import com.bennohan.mynotes.ui.editPassword.EditPasswordActivity
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.bennohan.mynotes.ui.register.RegisterActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.data.CoreSession
import com.crocodic.core.extension.openActivity
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding, LoginViewModel>(R.layout.activity_login) {

    @Inject
    lateinit var session: CoreSession


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvRegister()
        observe()

        binding.btnLoginBiometric.isVisible = session.getBoolean(Const.BIOMETRIC.BIOMETRIC)
        binding.tvOrLoginWith.isVisible = session.getBoolean(Const.BIOMETRIC.BIOMETRIC)


        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnLoginBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        binding.tvForgetPassword.setOnClickListener {
            openActivity<EditPasswordActivity>()
        }

    }

    private fun login() {
        val emailOrPhone = binding.etEmailPhone.textOf()
        val password = binding.etPassword.textOf()

        viewModel.login(emailOrPhone, password)
    }

    private fun tvRegister(){
        val spannableString = SpannableString("Don't Have an Account? Register Now")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                openActivity<RegisterActivity>()
            }
        }
        spannableString.setSpan(clickableSpan, 23, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvOption.text = spannableString
        binding.tvOption.movementMethod = LinkMovementMethod.getInstance() // Required for clickable spans to work

    }

    private fun showBiometricPrompt() {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Enter biometric credential to proceed")
            .setDescription("Input your Fingerprint or FaceID to ensure it's you!")
            .setNegativeButtonText("Cancel")

        val promptInfo = builder.build()

        val biometricPrompt = initBiometricPrompt {
            viewModel.login(session.getString(Const.LOGIN.EMAIL_PHONE), session.getString(Const.LOGIN.PASSWORD))
        }

        biometricPrompt.authenticate(promptInfo)
    }
    private fun initBiometricPrompt(listener: (Boolean) -> Unit): BiometricPrompt {
        //Get executor running on main thread.
        val executor = ContextCompat.getMainExecutor(this)

        //Creating object
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            //Override method to handle biometric result
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener(true)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener(false)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener(false)
            }
        }

        return BiometricPrompt(this, executor, callback)

    }



    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                openActivity<NavigationActivity> {
                                    finish()
                                }
                                tos("Success")
                            }
                            ApiStatus.ERROR -> {
                                disconnect(it)
                                loadingDialog.setResponse(it.message ?: return@collect)

                            }
                            else -> loadingDialog.setResponse(it.message ?: return@collect)
                        }

                    }
                }
            }
        }
    }
}
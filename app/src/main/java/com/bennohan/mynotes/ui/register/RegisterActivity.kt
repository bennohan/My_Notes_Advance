package com.bennohan.mynotes.ui.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.databinding.ActivityRegisterBinding
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.bennohan.mynotes.ui.login.LoginActivity
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.isEmptyRequired
import com.crocodic.core.extension.openActivity
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity :
    BaseActivity<ActivityRegisterBinding, RegisterViewModel>(R.layout.activity_register) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvLogin()
        validateRegister()
        observe()

        binding.btnRegister.setOnClickListener {
            register()
        }

    }

    //Text View Login
    private fun tvLogin() {
        val spannableString = SpannableString("Don't Have an Account? Login Now")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                openActivity<LoginActivity>()
            }
        }
        spannableString.setSpan(
            clickableSpan,
            23,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvOption.text = spannableString
        binding.tvOption.movementMethod =
            LinkMovementMethod.getInstance() // Required for clickable spans to work

    }

    private fun validateRegister() {
        binding.etPassword.doAfterTextChanged {
            validatePassword()
        }

        binding.etConfirmPassword.doAfterTextChanged {
            validatePassword()
            if (binding.etPassword.textOf().isEmpty()){
                binding.etPassword.error = "Password Tidak Boleh Kosong"
                binding.tvPasswordNotMatch.visibility = View.GONE
            }
        }
    }

    private fun isValidPasswordLength(password: String): Boolean {
        return password.length >= 6
    }

    private fun validatePassword(){
        if (!isValidPasswordLength(binding.etPassword.textOf())){
            binding.tvPasswordLength.visibility = View.VISIBLE
            return
        }else{
            binding.tvPasswordLength.visibility = View.GONE
        }
        if (binding.etPassword.textOf() != binding.etConfirmPassword.textOf()){
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            return
        }else{
            binding.tvPasswordNotMatch.visibility = View.GONE
        }
    }


    private fun register() {
        val name = binding.etName.textOf()
        val phone = binding.etPhone.textOf()
        val email = binding.etEmail.textOf()
        val password = binding.etPassword.textOf()
        val confirmPassword = binding.etConfirmPassword.textOf()


        if (binding.etName.isEmptyRequired(R.string.mustFill) || binding.etPhone.isEmptyRequired(R.string.mustFill)
            || binding.etPassword.isEmptyRequired(R.string.mustFillPassword) || binding.etConfirmPassword.isEmptyRequired(
                R.string.mustFillConfirmPass
            )
        ) {
            return
        }

        //TODO ADD Email Validation Function
        fun isValidIndonesianPhoneNumber(phoneNumber: String): Boolean {
            val regex = Regex("^\\+62\\d{9,15}$|^0\\d{9,11}$")
            return regex.matches(phoneNumber)
        }
        if (!isValidIndonesianPhoneNumber(phone)) {
            // Nomor Telephone valid
            binding.etPhone.error = "Nomor Telephone Tidak Valid"
            return
        }
        if (binding.etPassword.textOf() == binding.etConfirmPassword.textOf()) {
            // If Password is valid
            binding.tvPasswordNotMatch.visibility = View.GONE
            binding.tvPasswordNotMatch.visibility = View.GONE
        } else {
            // If Password Doesn't Valid
            binding.tvPasswordLength.visibility = View.GONE
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            return
        }

        viewModel.register(name, phone, email, password, confirmPassword)
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show("Register")
                            ApiStatus.SUCCESS -> {
                                tos("Register Success")
                                finish()
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
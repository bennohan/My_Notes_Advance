package com.bennohan.mynotes.ui.editPassword

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bennohan.mynotes.R
import com.bennohan.mynotes.base.BaseActivity
import com.bennohan.mynotes.databinding.ActivityEditPasswordBinding
import com.crocodic.core.api.ApiStatus
import com.crocodic.core.extension.isEmptyRequired
import com.crocodic.core.extension.textOf
import com.crocodic.core.extension.tos
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPasswordActivity :
    BaseActivity<ActivityEditPasswordBinding, EditPasswordViewModel>(R.layout.activity_edit_password) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observe()
        validatePassword()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEditPassword.setOnClickListener {
            editPassword()
        }

    }

    private fun validatePassword() {
        binding.etNewPassword.doAfterTextChanged {
            validatePasswordForm()
        }

        binding.etConfirmPassword.doAfterTextChanged {
            validatePasswordForm()
            if (binding.etNewPassword.textOf().isEmpty()) {
                binding.etNewPassword.error = "Password Tidak Boleh Kosong"
                binding.tvPasswordNotMatch.visibility = View.GONE
            }
        }

    }

    private fun isValidPasswordLength(password: String): Boolean {
        return password.length >= 6
    }

    private fun validatePasswordForm() {
        val newPassword = binding.etNewPassword.textOf()
        val confirmPassword = binding.etConfirmPassword.textOf()

        if (!isValidPasswordLength(newPassword)) {
            //If Password length is not 6 character
            binding.tvPasswordLength.visibility = View.VISIBLE
            return
        } else {
            binding.tvPasswordLength.visibility = View.GONE
        }
        if (confirmPassword != newPassword) {
            binding.tvPasswordNotMatch.visibility = View.VISIBLE
            return
        } else {
            binding.tvPasswordNotMatch.visibility = View.GONE
        }
    }


    private fun editPassword() {
        val newPassword = binding.etNewPassword.textOf()
        val confirmPassword = binding.etConfirmPassword.textOf()

        if (binding.etNewPassword.isEmptyRequired(R.string.mustFillPassword) || binding.etConfirmPassword.isEmptyRequired(
                R.string.mustFillConfirmPass
            )
        ) {
            return
        }

        viewModel.editPassword(newPassword, confirmPassword)
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.apiResponse.collect {
                        when (it.status) {
                            ApiStatus.LOADING -> loadingDialog.show()
                            ApiStatus.SUCCESS -> {
                                finish()
                                tos("Password Edited")
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
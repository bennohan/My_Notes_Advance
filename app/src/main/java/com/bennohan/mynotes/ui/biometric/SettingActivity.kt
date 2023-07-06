package com.bennohan.mynotes.ui.biometric

import android.os.Bundle
import androidx.biometric.BiometricManager
import com.bennohan.mynotes.R
import com.bennohan.mynotes.database.Const
import com.bennohan.mynotes.databinding.ActivityBiometricBinding
import com.crocodic.core.base.activity.NoViewModelActivity
import com.crocodic.core.data.CoreSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity :
    NoViewModelActivity<ActivityBiometricBinding>(R.layout.activity_biometric) {

    @Inject
    lateinit var session: CoreSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.hasBiometric = hasBiometricCapability()
        binding.enableBiometric = session.getBoolean(Const.BIOMETRIC.BIOMETRIC)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnBiometric.setOnCheckedChangeListener { buttonView, isChecked ->
            session.setValue(Const.BIOMETRIC.BIOMETRIC, isChecked)

        }

    }

    private fun hasBiometricCapability(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }
}

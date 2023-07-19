package com.bennohan.mynotes.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bennohan.mynotes.R
import com.bennohan.mynotes.database.user.UserDao
import com.bennohan.mynotes.databinding.ActivityMainBinding
import com.bennohan.mynotes.ui.home.NavigationActivity
import com.bennohan.mynotes.ui.login.LoginActivity
import com.crocodic.core.base.activity.NoViewModelActivity
import com.crocodic.core.extension.openActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : NoViewModelActivity<ActivityMainBinding>(R.layout.activity_main) {

    @Inject
    lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper())
            .postDelayed({
                lifecycleScope.launch {
                    val user = userDao.isLogin()
                    if (user) {
                        openActivity<NavigationActivity>()
                            finish()

                    } else {
                        binding.layoutLoginOption.visibility = View.VISIBLE
                        binding.btnLogin.setOnClickListener {
                            openActivity<LoginActivity>()
                                finish()

                        }
                    }
                }
            }, 5000)

    }
}
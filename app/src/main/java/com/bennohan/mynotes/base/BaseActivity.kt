package com.bennohan.mynotes.base

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.crocodic.core.base.activity.CoreActivity
import com.crocodic.core.base.viewmodel.CoreViewModel


open class BaseActivity<VB : ViewDataBinding , VM : CoreViewModel>(layoutRes: Int) : CoreActivity<VB,VM>(layoutRes) {

}
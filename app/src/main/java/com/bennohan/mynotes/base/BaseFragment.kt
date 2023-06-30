package com.bennohan.mynotes.base

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.crocodic.core.base.fragment.CoreFragment

open class BaseFragment <VB : ViewDataBinding>(@LayoutRes private val layoutRes: Int):
    CoreFragment<VB>(layoutRes){
    protected val app: App by lazy { requireActivity().application as App }

}
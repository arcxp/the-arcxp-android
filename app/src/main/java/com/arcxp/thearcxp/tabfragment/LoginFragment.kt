package com.arcxp.thearcxp.tabfragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.Failure
import com.arcxp.commerce.util.Success
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.CreateAccountFragment
import com.arcxp.thearcxp.databinding.FragmentLoginBinding
import com.arcxp.thearcxp.utils.showErrorDialog

class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var commerceManager: ArcXPCommerceManager
    private val forgotPassword = ForgotPasswordFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showSpinner(false)

        binding.newFacebookBtn.text = "   ${binding.facebookBtn.text}"
        commerceManager = ArcXPCommerceSDK.commerceManager()

        binding.newFacebookBtn.setOnClickListener {
            binding.facebookBtn.performClick()
        }

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            vm.rememberUser(isChecked)
        }

        //client must add 3rd party keys in local property to have access to login buttons
        if (getString(R.string.google_key).isBlank()) {
            binding.newGoogleBtn.visibility = GONE
        }

        if (getString(R.string.facebook_app_id).isBlank()) {
            binding.newFacebookBtn.visibility = GONE
        }

        binding.newGoogleBtn.setOnClickListener {
            vm.loginWithGoogle(activity as MainActivity, viewLifecycleOwner)
                .observe(viewLifecycleOwner) {
                    (activity as MainActivity).supportFragmentManager.popBackStack()
                    vm.restoreContentEvent()
                }
        }

        binding.facebookBtn.setOnClickListener {
            binding.facebookBtn.fragment = this
            vm.loginWithFacebook(binding.facebookBtn, viewLifecycleOwner)
                .observe(viewLifecycleOwner) {
                    (activity as MainActivity).supportFragmentManager.popBackStack()
                    vm.restoreContentEvent()
                }
        }

        binding.registerAccount.setOnClickListener {
            (activity as MainActivity).openFragment(
                CreateAccountFragment(),
                true,
                getString(R.string.create_account)
            )
        }

        binding.emailEt.doOnTextChanged { _, _, _, _ ->
            resetFields()
        }

        binding.passwordEt.doOnTextChanged { _, _, _, _ ->
            resetFields()
        }

        binding.forgotPassword.setOnClickListener {
            (activity as MainActivity).openFragment(
                forgotPassword,
                true,
                getString(R.string.forgot_password)
            )
        }

        binding.signInBtn.setOnClickListener {
            showSpinner(true)
            vm.login(
                binding.emailEt.text.toString(),
                binding.passwordEt.text.toString(), viewLifecycleOwner
            ).observe(viewLifecycleOwner) {
                when (it) {
                    is Success -> {
                        showSpinner(false)
                        (activity as MainActivity).supportFragmentManager.popBackStack()
                        vm.restoreContentEvent()
                        binding.emailEt.text.clear()
                        binding.passwordEt.text.clear()
                    }
                    is Failure -> showError(it.l!!)
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        commerceManager.onActivityResults(
            requestCode,
            resultCode,
            data,
            object : ArcXPIdentityListener() {
                override fun onLoginSuccess(response: ArcXPAuth) {
                }

                override fun onLoginError(error: ArcXPError) {
                }
            })
    }

    private fun showError(error: ArcXPError) {
        showSpinner(false)
        binding.email.setBackgroundResource(R.drawable.error_outline)
        binding.password.setBackgroundResource(R.drawable.error_outline)
        binding.emailTv.setTextColor(resources.getColor(R.color.red))
        binding.emailEt.setTextColor(resources.getColor(R.color.red))
        binding.passwordTv.setTextColor(resources.getColor(R.color.red))
        binding.passwordEt.setTextColor(resources.getColor(R.color.red))
        binding.errorMessage.visibility = VISIBLE
        requireActivity().showErrorDialog(error.type?.name!!, error.localizedMessage)
    }

    private fun resetFields() {
        binding.email.setBackgroundResource(R.drawable.outline_shape)
        binding.password.setBackgroundResource(R.drawable.outline_shape)
        binding.emailTv.setTextColor(Color.BLACK)
        binding.emailEt.setTextColor(Color.BLACK)
        binding.passwordTv.setTextColor(Color.BLACK)
        binding.passwordEt.setTextColor(Color.BLACK)
        binding.errorMessage.visibility = GONE
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.loginFragment,
            R.id.loginCL,
            true
        )
    }

    private fun showSpinner(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
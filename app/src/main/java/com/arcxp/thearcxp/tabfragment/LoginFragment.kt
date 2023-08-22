package com.arcxp.thearcxp.tabfragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPAuth
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.CreateAccountFragment
import com.arcxp.thearcxp.databinding.FragmentLoginBinding
import com.arcxp.thearcxp.utils.showErrorDialog


class LoginFragment : BaseFragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val commerceManager = ArcXPMobileSDK.commerceManager()
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

        binding.loginNewFacebookBtn.text = "${binding.loginFacebookBtn.text}"

        binding.loginNewFacebookBtn.setOnClickListener {
            binding.loginFacebookBtn.performClick()
        }

        binding.loginRememberMeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            vm.rememberUser(isChecked)
        }

        //client must add 3rd party keys in local property to have access to login buttons
        if (getString(R.string.google_key).isBlank()) {
            binding.loginNewGoogleBtn.visibility = GONE
        }

        if (getString(R.string.facebook_app_id).isBlank()) {
            binding.loginNewFacebookBtn.visibility = GONE
        }

        binding.loginNewGoogleBtn.setOnClickListener {
            vm.loginWithGoogle(activity as MainActivity, viewLifecycleOwner)
                .observe(viewLifecycleOwner) {
                    parentFragmentManager.popBackStack()
                    vm.restoreContentEvent()
                }
        }

        binding.loginFacebookBtn.setOnClickListener {
            binding.loginFacebookBtn.fragment = this
            vm.loginWithFacebook(binding.loginFacebookBtn, viewLifecycleOwner)
                .observe(viewLifecycleOwner) {
                    parentFragmentManager.popBackStack()
                    vm.restoreContentEvent()
                }
        }

        binding.loginRegisterAccountButton.setOnClickListener {
            (activity as MainActivity).openFragment(
                CreateAccountFragment(),
                true,
                getString(R.string.create_account)
            )
        }

        binding.loginForgotPasswordButton.setOnClickListener {
            (activity as MainActivity).openFragment(
                forgotPassword,
                true,
                getString(R.string.forgot_password)
            ) //TODO send this through view model
        }

        binding.loginButton.setOnClickListener {
            if (validateInput()) {
                showSpinner(visible = true)
                vm.login(
                    binding.loginEmailEdit.text.toString(),
                    binding.loginPasswordEdit.text.toString(), viewLifecycleOwner
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        is Success -> {
                            showSpinner(visible = false)
                            (activity as MainActivity).supportFragmentManager.popBackStack()
                            vm.restoreContentEvent()
                            binding.loginEmailEdit.text.clear()
                            binding.loginPasswordEdit.text.clear()
                        }
                        is Failure -> showError(error = it.failure)
                    }
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

                override fun onLoginError(error: ArcXPException) {
                }
            })
    }

    private fun showError(error: ArcXPException) {
        showSpinner(false)
        binding.errorMessage.visibility = VISIBLE
        requireActivity().showErrorDialog(
            title = error.type?.name ?: getString(R.string.error),
            message = error.localizedMessage
        )
    }

    private fun validateInput() : Boolean {
        var valid = true
        if (binding.loginEmailEdit.text.isBlank()) {
            binding.loginEmailEdit.error = getString(R.string.email_error)
            valid = false
        }
        if (binding.loginPasswordEdit.text.isBlank()) {
            binding.loginPasswordEdit.error = getString(R.string.password_error)
            valid = false
        }
        return valid
    }

    private fun onError(error: ArcXPException) {
        showSnackBar(
            error = error,
            view = binding.loginFragment,
            viewId = R.id.loginCL
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
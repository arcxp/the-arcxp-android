package com.arcxp.thearcxp.tabfragment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPRequestPasswordReset
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : BaseFragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var commerceManager: ArcXPCommerceManager
    private var success = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        commerceManager = ArcXPCommerceSDK.commerceManager()

        binding.resetEmailButton.setOnClickListener {
            if (success) {
                success = false
                (activity as MainActivity).supportFragmentManager.popBackStack()
            } else {
                showSpinner(true)
                commerceManager.requestResetPassword(
                    binding.resetEmailEdit.text.toString(),
                    object : ArcXPIdentityListener() {
                        override fun onPasswordResetNonceSuccess(response: ArcXPRequestPasswordReset?) {
                            showSpinner(false)
                            binding.forgotPasswordMessage.text =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Html.fromHtml(
                                        getString(
                                            R.string.password_success,
                                            binding.resetEmailEdit.text
                                        ),
                                        Build.VERSION.SDK_INT
                                    )
                                } else {
                                    Html.fromHtml(
                                        getString(
                                            R.string.password_success,
                                            binding.resetEmailEdit.text
                                        )
                                    )

                                }
                            binding.resetEmailEdit.visibility = GONE
                            binding.resetEmailLabel.visibility = GONE
                            binding.resetEmailButton.text = getString(R.string.back_to_sign_in)
                            success = true
                        }

                        override fun onPasswordResetNonceFailure(error: ArcXPError) {
                            showSpinner(false)
                        }
                    })
            }
        }

    }

    private fun showSpinner(visible: Boolean) {
        binding.forgotPasswordSpinner.isVisible = visible
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            error = error,
            view = binding.root,
            viewId = R.id.loginCL
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
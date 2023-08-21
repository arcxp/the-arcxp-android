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

        binding.resetEmailBtn.setOnClickListener {
            if (success) {
                success = false
                (activity as MainActivity).supportFragmentManager.popBackStack()
            } else {
                showSpinner(true)
                commerceManager.requestResetPassword(
                    binding.resetEmailEt.text.toString(),
                    object : ArcXPIdentityListener() {
                        override fun onPasswordResetNonceSuccess(response: ArcXPRequestPasswordReset?) {
                            showSpinner(false)
                            binding.messageTv.text =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    Html.fromHtml(
                                        "Success! A password reset email link has been sent to:<br><b>${binding.resetEmailEt.text}",
                                        Build.VERSION.SDK_INT
                                    )
                                } else {
                                    Html.fromHtml("Success! A password reset email link has been sent to:<br><b>${binding.resetEmailEt.text}")
                                }
                            binding.resetEmail.visibility = GONE
                            binding.resetEmailBtn.text = "Back to Sign In"
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
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.root,
            R.id.loginCL,
            true
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
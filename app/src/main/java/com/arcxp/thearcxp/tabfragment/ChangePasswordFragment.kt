package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentChangePasswordBinding
import com.arcxp.thearcxp.utils.showErrorDialog

class ChangePasswordFragment : BaseFragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newPasswordEt.doOnTextChanged { _, _, _, _ ->
            if (binding.confirmPasswordEt.text.isNotEmpty()) {
                match()
            } else {
                binding.updatePasswordBtn.isEnabled = false
                binding.newPassword.setBackgroundResource(R.drawable.outline_shape)
                binding.confirmPassword.setBackgroundResource(R.drawable.outline_shape)
                binding.errorMessageTv.visibility = GONE
            }
        }

        binding.confirmPasswordEt.doOnTextChanged { _, _, _, _ ->
            match()
        }

        binding.updatePasswordBtn.setOnClickListener {
            vm.updatePassword(
                binding.newPasswordEt.text.toString(),
                binding.oldPasswordEt.text.toString(),
                object : ArcXPIdentityListener() {
                    override fun onPasswordChangeSuccess(it: ArcXPIdentity) {
                        (activity as MainActivity).supportFragmentManager.popBackStack()
                    }

                    override fun onPasswordChangeError(error: ArcXPError) {
                        arcError(error)
                    }
                })
        }
    }

    private fun match(): Boolean {
        if (binding.confirmPasswordEt.text.toString() != binding.newPasswordEt.text.toString()) {
            binding.updatePasswordBtn.isEnabled = false
            binding.newPassword.setBackgroundResource(R.drawable.error_outline)
            binding.confirmPassword.setBackgroundResource(R.drawable.error_outline)
            binding.errorMessageTv.visibility = VISIBLE
            return false
        } else {
            binding.updatePasswordBtn.isEnabled = true
            binding.newPassword.setBackgroundResource(R.drawable.outline_shape)
            binding.confirmPassword.setBackgroundResource(R.drawable.outline_shape)
            binding.errorMessageTv.visibility = GONE
        }
        return true
    }

    private fun arcError(error: ArcXPError) {
        requireActivity().showErrorDialog(message = error.localizedMessage)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.updatePasswordBtn.isEnabled = false
        binding.confirmPasswordEt.text.clear()
        binding.newPasswordEt.text.clear()
        binding.oldPasswordEt.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
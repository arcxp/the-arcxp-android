package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        binding.newPasswordEdit.doOnTextChanged { _, _, _, _ ->
            if (binding.confirmPasswordEdit.text.isNotEmpty()) {
                match()
            } else {
                binding.updatePasswordButton.isEnabled = false
                binding.newPasswordLabel.setBackgroundResource(R.drawable.outline_shape)
                binding.confirmPasswordLabel.setBackgroundResource(R.drawable.outline_shape)
                binding.changePasswordErrorMessage.visibility = GONE
            }
        }

        binding.confirmPasswordEdit.doOnTextChanged { _, _, _, _ ->
            match()
        }

        binding.updatePasswordButton.setOnClickListener {
            vm.updatePassword(
                binding.newPasswordEdit.text.toString(),
                binding.oldPasswordEdit.text.toString(),
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
        if (binding.confirmPasswordEdit.text.toString() != binding.newPasswordEdit.text.toString()) {
            val errorColor = ContextCompat.getColor(requireContext(), R.color.error)
            binding.updatePasswordButton.isEnabled = false
            binding.confirmPasswordEdit.isSelected = true
            binding.newPasswordEdit.isSelected = true
            binding.newPasswordLabel.setTextColor(errorColor)
            binding.confirmPasswordLabel.setTextColor(errorColor)
            binding.newPasswordEdit.setTextColor(errorColor)
            binding.confirmPasswordEdit.setTextColor(errorColor)
            binding.changePasswordErrorMessage.visibility = VISIBLE
            return false
        } else {

            val normalTextColor = ContextCompat.getColor(requireContext(), R.color.text)
            val editTextColor = ContextCompat.getColor(requireContext(), R.color.edit_text)
            binding.updatePasswordButton.isEnabled = true
            binding.newPasswordLabel.setTextColor(normalTextColor)
            binding.confirmPasswordLabel.setTextColor(normalTextColor)
            binding.newPasswordEdit.setTextColor(editTextColor)
            binding.confirmPasswordEdit.setTextColor(editTextColor)
            binding.confirmPasswordEdit.isSelected = false
            binding.newPasswordEdit.isSelected = false
            binding.changePasswordErrorMessage.visibility = GONE
        }
        return true
    }

    private fun arcError(error: ArcXPError) {
        requireActivity().showErrorDialog(message = error.localizedMessage)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.updatePasswordButton.isEnabled = false
        binding.confirmPasswordEdit.text.clear()
        binding.newPasswordEdit.text.clear()
        binding.oldPasswordEdit.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
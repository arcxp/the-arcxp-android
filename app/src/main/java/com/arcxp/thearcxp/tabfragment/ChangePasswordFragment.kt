package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPIdentity
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentChangePasswordBinding
import com.arcxp.thearcxp.utils.showErrorDialog

class ChangePasswordFragment : BaseFragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private var lowercase = false
    private var uppercase = false
    private var special = false
    private var length = false
    private var numerical = false
    private var textColorSuccess: Int = 0
    private var textColorError: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textColorSuccess = requireContext().getColor(R.color.success)
        textColorError = requireContext().getColor(R.color.error)


        binding.newPasswordEdit.doOnTextChanged { _, _, _, _ ->
            validate()
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
                        clearForm()
                        (activity as MainActivity).supportFragmentManager.popBackStack()
                    }

                    override fun onPasswordChangeError(error: ArcXPException) {
                        arcError(error)
                    }
                })
        }
    }

    private fun match(): Boolean {
        if (binding.confirmPasswordEdit.text.toString() != binding.newPasswordEdit.text.toString()) {
            binding.updatePasswordButton.isEnabled = false
            binding.changePasswordErrorMessage.visibility = VISIBLE
            return false
        } else {
            val valid = lowercase && uppercase && special && length && numerical
            binding.updatePasswordButton.isEnabled = valid
            binding.changePasswordErrorMessage.visibility = GONE
            return true
        }
    }

    private fun arcError(error: ArcXPException) {
        requireActivity().showErrorDialog(
            title = error.localizedMessage,
            getString(R.string.check_username_password)
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.updatePasswordButton.isEnabled = false
        clearForm()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun clearForm() {
        binding.confirmPasswordEdit.text.clear()
        binding.newPasswordEdit.text.clear()
        binding.oldPasswordEdit.text.clear()
    }

    private fun validate() {
        lowercase =
            if (binding.newPasswordEdit.text.contains(Regex(getString(R.string.lowercase_pattern)))) {
                binding.pwLowercase.setTextColor(textColorSuccess)
                true
            } else {
                binding.pwLowercase.setTextColor(textColorError)
                false
            }
        uppercase =
            if (binding.newPasswordEdit.text.contains(Regex(getString(R.string.uppercase_pattern)))) {
                binding.pwUppercase.setTextColor(textColorSuccess)
                true
            } else {
                binding.pwUppercase.setTextColor(textColorError)
                false
            }
        numerical =
            if (binding.newPasswordEdit.text.contains(Regex(getString(R.string.numerical_pattern)))) {
                binding.pwNumber.setTextColor(textColorSuccess)
                true
            } else {
                binding.pwNumber.setTextColor(textColorError)
                false
            }
        special =
            if (binding.newPasswordEdit.text.contains(Regex(getString(R.string.special_pattern)))) {
                binding.pwSpecial.setTextColor(textColorSuccess)
                true
            } else {
                binding.pwSpecial.setTextColor(textColorError)
                false
            }
        length = if (binding.newPasswordEdit.text.length >= 6) {
            binding.pwLength.setTextColor(textColorSuccess)
            true
        } else {
            binding.pwLength.setTextColor(textColorError)
            false
        }
        match()
    }

}
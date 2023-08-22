package com.arcxp.thearcxp.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.Success
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentSignupBinding
import com.arcxp.thearcxp.utils.isValidEmail
import com.arcxp.thearcxp.utils.showErrorDialog
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private var textColorSuccess: Int = 0
    private var textColorError: Int = 0

    private val vm: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textColorSuccess = requireContext().getColor(R.color.success)
        textColorError = requireContext().getColor(R.color.error)
        showSpinner(false)
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.GONE
        showSpinner(false)

        binding.passwordEdit.doOnTextChanged { _, _, _, _ ->
            validatePassword()
        }



        binding.signUpButton.setOnClickListener {
            if (checkInputs()) {
                showSpinner(true)
                vm.signUp(
                    username = binding.emailEdit.text.toString(),
                    firstname = binding.firstNameEdit.text.toString(),
                    lastname = binding.lastNameEdit.text.toString(),
                    email = binding.emailEdit.text.toString(),
                    password = binding.passwordEdit.text.toString()
                ).observe(viewLifecycleOwner) {
                    showSpinner(false)
                    when(it){
                        is Success -> {
                            requireActivity().supportFragmentManager.popBackStack()
                            (requireActivity() as MainActivity).openFragment(
                                SignUpSuccessFragment(),
                                tag = getString(R.string.signup_successful)
                            )
                        }
                        is Failure -> {
                            requireActivity().showErrorDialog(
                                message = it.failure.localizedMessage
                            )
                        }
                    }




                }
            }
        }

        binding.signUpSignInButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToSignIn()
        }
    }

    private fun checkInputs(): Boolean {
        var valid = true
        if (binding.firstNameEdit.text.isBlank()) {
            binding.firstNameEdit.error = getString(R.string.first_name_error)
            valid = false
        }
        if (binding.lastNameEdit.text.isBlank()) {
            binding.lastNameEdit.error = getString(R.string.last_name_error)
            valid = false
        }
        if (!binding.emailEdit.text.isValidEmail()) {
            binding.emailEdit.error = getString(R.string.email_error)
            valid = false
        }
        if (binding.passwordEdit.text.isBlank()) {
            binding.passwordEdit.error = getString(R.string.password_error)
            valid = false
        }
        return valid
    }

    private fun showSpinner(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    private fun validatePassword() {

        if (binding.passwordEdit.text.contains(Regex(getString(R.string.lowercase_pattern)))) {
            binding.pwLowercase.setTextColor(textColorSuccess)

        } else {
            binding.pwLowercase.setTextColor(textColorError)

        }
        if (binding.passwordEdit.text.contains(Regex(getString(R.string.uppercase_pattern)))) {
            binding.pwUppercase.setTextColor(textColorSuccess)

        } else {
            binding.pwUppercase.setTextColor(textColorError)

        }
        if (binding.passwordEdit.text.contains(Regex(getString(R.string.numerical_pattern)))) {
            binding.pwNumber.setTextColor(textColorSuccess)

        } else {
            binding.pwNumber.setTextColor(textColorError)

        }
        if (binding.passwordEdit.text.contains(Regex(getString(R.string.special_pattern)))) {
            binding.pwSpecial.setTextColor(textColorSuccess)

        } else {
            binding.pwSpecial.setTextColor(textColorError)

        }
        if (binding.passwordEdit.text.length >= 6) {
            binding.pwLength.setTextColor(textColorSuccess)

        } else {
            binding.pwLength.setTextColor(textColorError)

        }


    }


    override fun onStop() {
        super.onStop()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
    }
}


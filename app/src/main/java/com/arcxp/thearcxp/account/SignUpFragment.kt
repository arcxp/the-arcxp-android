package com.arcxp.thearcxp.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentSignupBinding
import com.arcxp.thearcxp.utils.showAlertDialog
import com.arcxp.thearcxp.utils.showErrorDialog
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

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
        showSpinner(false)
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.GONE
        showSpinner(false)

        binding.signUpButton.setOnClickListener {
            if (checkInputs()) {
                vm.commerceErrors().observe(viewLifecycleOwner) {
                    showSpinner(false)
                    requireActivity().showErrorDialog(
                        message = it.localizedMessage
                    )
                }

                showSpinner(true)
                vm.signUp(
                    username = binding.emailEdit.text.toString(),
                    firstname = binding.firstNameEdit.text.toString(),
                    lastname = binding.lastNameEdit.text.toString(),
                    email = binding.emailEdit.text.toString(),
                    password = binding.passwordEdit.text.toString()
                ).observe(viewLifecycleOwner) {
                    showSpinner(false)
                    requireActivity().supportFragmentManager.popBackStack()
                    (requireActivity() as MainActivity).openFragment(
                        SignUpSuccessFragment(),
                        tag = getString(R.string.signup_successful)
                    )
                }
            } else {
                requireActivity().showAlertDialog(
                    message = "Please enter all values.",
                    posBtnTxt = "OK",
                    posAction = {}
                )
            }
        }

        binding.signUpSignInButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToSignIn()
        }
    }

    private fun checkInputs(): Boolean {
        return binding.emailEdit.text.isNotBlank() &&
                binding.firstNameEdit.text.isNotBlank() && binding.lastNameEdit.text.isNotBlank()
                && binding.passwordEdit.text.isNotBlank()
    }

    private fun showSpinner(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
    }
}
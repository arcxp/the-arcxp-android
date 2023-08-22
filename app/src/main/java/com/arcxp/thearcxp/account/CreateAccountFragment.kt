package com.arcxp.thearcxp.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.databinding.FragmentCreateAccountBinding
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!

    val vm: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.GONE

        binding.signUpButton.setOnClickListener {
            (requireActivity() as MainActivity).openFragment(
                SignUpFragment(),
                true,
                tag = getString(R.string.sign_up)
            )
        }

        binding.facebookButton.setOnClickListener {
            binding.btnFacebook.performClick()
        }

        binding.btnFacebook.setOnClickListener {
            ArcXPCommerceSDK.commerceManager()
                .loginWithFacebook(binding.btnFacebook)
                .observe(viewLifecycleOwner)
                {
                    (requireActivity() as MainActivity).navigateToSignIn()
                }
        }

        binding.googleButton.setOnClickListener {
            ArcXPCommerceSDK.commerceManager()
                .loginWithGoogle(requireActivity() as MainActivity)
                .observe(viewLifecycleOwner)
                {
                    (requireActivity() as MainActivity).navigateToSignIn()
                }
        }

        binding.createAccountSignInButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToSignIn()
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
    }
}
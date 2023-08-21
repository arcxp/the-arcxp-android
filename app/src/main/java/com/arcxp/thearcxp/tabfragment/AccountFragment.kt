package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.arc.arcvideo.ArcXPVideoSDK
import com.arcxp.commerce.ArcXPCommerceSDK
import com.arcxp.commerce.apimanagers.ArcXPIdentityListener
import com.arcxp.commerce.models.ArcXPProfileManage
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.models.ArcXPContentError
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.account.CreateAccountFragment
import com.arcxp.thearcxp.databinding.FragmentAccountBinding
import com.arcxp.thearcxp.utils.showErrorDialog


class AccountFragment : BaseFragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private var changePasswordFragment = ChangePasswordFragment()
    private var createAccountFragment = CreateAccountFragment()
    private val loginFragment = LoginFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!ArcXPCommerceSDK.isInitialized()) {
            binding.header.visibility = GONE
            binding.loginLayout.visibility = GONE
            binding.createLayout.visibility = GONE

        } else {
            vm.isLoggedIn().observe(viewLifecycleOwner) {
                when (it) {
                    true -> getUserData()
                    else -> {}
                }
            }

            binding.loginLayout.setOnClickListener {
                if (ArcXPCommerceSDK.commerceManager().sessionIsActive()) {
                    vm.logout(object : ArcXPIdentityListener() {
                        override fun onLogoutSuccess() {
                            binding.usernameTv.visibility = GONE
                            binding.loginTv.text = "Login"
                            binding.createAccountTv.text = "Create Account"
                        }

                        override fun onLogoutError(error: ArcXPError) {
                            requireActivity().showErrorDialog(
                                error.type?.name!!,
                                error.localizedMessage
                            )
                        }
                    })
                } else {
                    (requireActivity() as MainActivity).openFragment(
                        loginFragment,
                        true,
                        getString(R.string.login)
                    )
                }
            }

            if (!ArcXPCommerceSDK.commerceManager().sessionIsActive()) {
                vm.logout(object : ArcXPIdentityListener() {
                    override fun onLogoutSuccess() {}
                })
            }

            binding.createLayout.setOnClickListener {
                if (ArcXPCommerceSDK.commerceManager().sessionIsActive()) {
                    (requireActivity() as MainActivity).openFragment(
                        changePasswordFragment,
                        true,
                        getString(R.string.change_password)
                    )
                } else {
                    (requireActivity() as MainActivity).openFragment(
                        createAccountFragment,
                        true,
                        getString(R.string.create_account)
                    )
                }
            }
        }

        binding.contentSdkVersion.text =
            "Content: ${ArcXPContentSDK.getVersion(requireContext())}"
        binding.commerceSdkVersion.text =
            "Commerce: ${ArcXPCommerceSDK.getVersion(requireContext())}"
        binding.videoSdkVersion.text = "Video: ${ArcXPVideoSDK.getVersion(requireContext())}"

        binding.tosLayout.setOnClickListener {
            openFragment(
                WebSectionFragment().withUrlAndName(
                    getString(R.string.tos_url),
                    "Terms of Service"
                ), getString(R.string.web_section)
            )
        }

        binding.ppLayout.setOnClickListener {
            openFragment(
                WebSectionFragment().withUrlAndName(
                    getString(R.string.pp_url),
                    "Privacy Policy"
                ), getString(R.string.web_section)
            )
        }
    }

    private fun getUserData(){
        vm.getUserProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                showProfile(profileResponse)
            }

            override fun onProfileError(error: ArcXPError) {
                requireActivity().showErrorDialog(
                    error.type?.name!!,
                    error.localizedMessage
                )
            }
        })
    }
    
    private fun showProfile(arcXPProfileManage: ArcXPProfileManage) {
        binding.loginTv.text = "Logout"
        binding.createAccountTv.text = "Change Password"
        binding.usernameTv.text = "${arcXPProfileManage.firstName} ${arcXPProfileManage.lastName}"
        binding.usernameTv.visibility = VISIBLE
    }

    private fun onError(error: ArcXPContentError) {
        showSnackBar(
            ArcXPContentError(error.type!!, error.localizedMessage),
            binding.root,
            R.id.account_frag,
            true
        )
    }

    private fun openFragment(
        fragment: Fragment,
        tag: String,
    ) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .add((requireActivity() as MainActivity).getFragmentContainerViewId(), fragment, tag)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
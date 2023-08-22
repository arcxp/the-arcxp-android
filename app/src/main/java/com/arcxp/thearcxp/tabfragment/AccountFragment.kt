package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commerce.callbacks.ArcXPIdentityListener
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commons.throwables.ArcXPException
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

        if (!ArcXPMobileSDK.commerceInitialized()) {
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
                if (ArcXPMobileSDK.commerceManager().sessionIsActive()) {
                    vm.logout(object : ArcXPIdentityListener() {
                        override fun onLogoutSuccess() {
                            binding.userName.visibility = GONE
                            binding.loginText.text = getString(R.string.login)
                            binding.createAccountText.text = getString(R.string.create_account)
                        }

                        override fun onLogoutError(error: ArcXPException) {
                            requireActivity().showErrorDialog(
                                title = error.type?.name ?: getString(R.string.error),
                                message = error.localizedMessage
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

            if (!ArcXPMobileSDK.commerceManager().sessionIsActive()) {
                vm.logout(object : ArcXPIdentityListener() {
                    override fun onLogoutSuccess() {}
                })
            }

            binding.createLayout.setOnClickListener {
                if (ArcXPMobileSDK.commerceManager().sessionIsActive()) {
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

        binding.sdkVersion.text = getString(
            R.string.SDK_colon_version,
            ArcXPMobileSDK.getVersion(requireContext().applicationContext)
        )

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
                    getString(R.string.privacy_policy)
                ), getString(R.string.web_section)
            )
        }
    }

    private fun getUserData() {
        vm.getUserProfile(object : ArcXPIdentityListener() {
            override fun onFetchProfileSuccess(profileResponse: ArcXPProfileManage) {
                showProfile(profileResponse)
            }

            override fun onProfileError(error: ArcXPException) {
                requireActivity().showErrorDialog(
                    title = error.type?.name ?: getString(R.string.error),
                    message = error.localizedMessage
                )
            }
        })
    }

    private fun showProfile(arcXPProfileManage: ArcXPProfileManage) {
        binding.loginText.text = getString(R.string.logout)
        binding.createAccountText.text = getString(R.string.change_password)
        binding.userName.text =
            getString(R.string.user_name, arcXPProfileManage.firstName, arcXPProfileManage.lastName)
        binding.userName.visibility = VISIBLE
    }

    private fun onError(error: ArcXPException) {
        showSnackBar(
            error = error,
            view = binding.root,
            viewId = R.id.account_frag
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
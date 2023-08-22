package com.arcxp.thearcxp.utils

import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.arcxp.thearcxp.MainActivity
import com.arcxp.thearcxp.R
import com.arcxp.thearcxp.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Dialog to show paywall.  This dialog can either be dismissed or it can take the
 * user to the sign in/sign up flow.
 */
class Paywall : DialogFragment() {

    //Listener to indicate if the dialog was cancelled or if the registration process
    //was invoked.  This way the calling fragment can know how to respond.
    private var paywallListener: PaywallListener? = null
    private val vm: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        return activity?.let {
            val bottomSheetDialog = BottomSheetDialog(requireActivity())
            val inflater = requireActivity().layoutInflater
            val paywallView = inflater.inflate(R.layout.fragment_paywall, null)
            val subscribe = paywallView.findViewById<Button>(R.id.subscribe_btn)
            val signIn = paywallView.findViewById<TextView>(R.id.paywall_sign_in_button)
            val exit = paywallView.findViewById<ImageView>(R.id.exit_button)
            bottomSheetDialog.setContentView(paywallView)
            bottomSheetDialog.setCanceledOnTouchOutside(false)
            exit.setOnClickListener {
                vm.disposeVideoPlayer()
                dismiss()
            }
            subscribe.setOnClickListener {
                dismiss()
                //triggerPaywall
                (requireActivity() as MainActivity).navigateToCreateAccount()
            }
            signIn.setOnClickListener {
                dismiss()
                (requireActivity() as MainActivity).navigateToSignIn()
            }
            paywallListener?.onPaywallShow()
            bottomSheetDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        paywallListener?.onPaywallCancel()
        parentFragmentManager.popBackStack()
    }

    fun setOnPaywallCancelledListener(listener: PaywallListener) {
        paywallListener = listener
    }

    interface PaywallListener {
        fun onPaywallShow()
        fun onPaywallCancel()
    }
}
package com.arcxp.thearcxp.tabfragment

import android.content.res.Configuration
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.thearcxp.R
import com.google.android.material.snackbar.Snackbar


open class BaseFragment : Fragment() {
//    val vm: MainViewModel by activityViewModels()
//    val accountViewModel: AccountViewModel by activityViewModels()

    private var snackBarError: Snackbar? = null

    fun dismissSnackBar() {
        snackBarError?.dismiss()
    }

    fun showSnackBar(
        error: ArcXPException,
        view: View,
        viewId: Int,
        dismissible: Boolean = true,
        onDismiss: () -> Unit = {}
    ) {
        val message = SpannableStringBuilder()
            .bold { append("${error.type?.name}:\n") }
            .append(error.message)
        val snackBar =
            Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        if (dismissible) {
            snackBar.setAction(getString(R.string.dismiss)) { onDismiss() }
        }
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.view.setBackgroundColor(Color.argb(225, 255, 0, 0))
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            5

        if (view.layoutParams is CoordinatorLayout.LayoutParams) {
            val layoutParams = snackBar.view.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.anchorId = viewId
            layoutParams.anchorGravity = Gravity.CENTER_HORIZONTAL
            layoutParams.width = CoordinatorLayout.LayoutParams.MATCH_PARENT
        } else {
//            val appBarView =
//                (activity as MainActivity).findViewById<TextView>(R.id.appTitleTextView)
//            val appBarParams = appBarView.layoutParams
            val frameLayoutParams = snackBar.view.layoutParams as FrameLayout.LayoutParams
//            frameLayoutParams.topMargin = appBarParams.height
            frameLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
            frameLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        }

        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && view.layoutParams is FrameLayout.LayoutParams) {
            val frameLayoutParams = snackBar.view.layoutParams as FrameLayout.LayoutParams
            frameLayoutParams.topMargin = 0
            frameLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
            frameLayoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        }
        snackBar.view.elevation = 150F
        this.snackBarError = snackBar
        snackBar.show()
    }
}
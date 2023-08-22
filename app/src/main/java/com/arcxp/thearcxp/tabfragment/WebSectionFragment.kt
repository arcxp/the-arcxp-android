package com.arcxp.thearcxp.tabfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.arcxp.thearcxp.databinding.FragmentWebSectionBinding

/**
 * Single section fragment with WebView. You need to provide the section name and a valid full URL to a web page
 */
class WebSectionFragment : BaseSectionFragment() {

    private var _binding: FragmentWebSectionBinding? = null
    private val binding get() = _binding!!

    /**
     * @url full URL to a web page
     * @name section name
     */
    fun withUrlAndName(url: String?, name: String?): WebSectionFragment {
        val arg = arguments ?: Bundle()
        arg.putString(ARG_URL, url)
        arg.putString(ARG_NAME, name)
        arguments = arg
        return this
    }

    override fun getSectionName(): String {
        return arguments?.getString(ARG_NAME) ?: ""
    }

    private fun getUrl(): String {
        return arguments?.getString(ARG_URL) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebSectionBinding.inflate(inflater, container, false)

        binding.progressBar.progress = 0
        binding.pullToRefreshView.setOnRefreshListener {
            binding.webView.reload()
            binding.webView.postDelayed({ binding.pullToRefreshView.isRefreshing = false }, 10_000)
        }
        binding.webView.webChromeClient = createWebChromeClient()
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.allowContentAccess = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.loadUrl(getUrl())

        return binding.root
    }

    private fun createWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {

            var customView: View? = null
            private var originalSystemUiVisibility: Int = 0
            private var originalOrientation: Int = 0
            private var customViewCallback: CustomViewCallback? = null

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                    binding.pullToRefreshView.isRefreshing = false
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                super.onShowCustomView(view, callback)
                customView = view
                val activity = activity ?: return

                originalSystemUiVisibility = activity.window.decorView.systemUiVisibility
                originalOrientation = activity.requestedOrientation
                customViewCallback = callback
                (activity.window.decorView as FrameLayout).addView(customView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                activity.window.decorView.systemUiVisibility =
                        SYSTEM_UI_FLAG_IMMERSIVE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_HIDE_NAVIGATION
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                val activity = activity ?: return
                (activity.window.decorView as FrameLayout).removeView(customView)
                customView = null
                activity.window.decorView.systemUiVisibility = this.originalSystemUiVisibility
                activity.requestedOrientation = this.originalOrientation
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.webView.destroy()
        _binding = null
    }

    companion object {
        const val ARG_URL = "ARG_URL"
        const val ARG_NAME = "ARG_NAME"
    }
}

package com.awscherb.cardkeeper.ui.scan

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.PermissionChecker
import androidx.navigation.fragment.findNavController
import com.awscherb.cardkeeper.R
import com.awscherb.cardkeeper.data.model.ScannedCode
import com.awscherb.cardkeeper.ui.base.BaseFragment
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.android.synthetic.main.fragment_scan.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ScanFragment : BaseFragment(), ScanContract.View {

    @Inject
    lateinit var presenter: ScanContract.Presenter

    private val found = AtomicBoolean(false)

    //================================================================================
    // Lifecycle methods
    //================================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_scan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewComponent.inject(this)
        presenter.attachView(this)

        // Check permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity?.checkSelfPermission(Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
                scanScanner.resume()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA
                )
            }
        }

        // Setup scanner
        setCallback()
    }

    override fun onResume() {
        super.onResume()
        scanScanner.resume()
    }

    override fun onPause() {
        super.onPause()
        scanScanner.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onViewDestroyed()
    }

    // ========================================================================
    // View methods
    // ========================================================================

    override fun onCodeAdded() {
        findNavController().navigate(
            ScanFragmentDirections.actionScanFragmentToCardsFragment()
        )
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    private fun setCallback() {
        val callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (found.compareAndSet(false, true)) {
                    val scannedCode = ScannedCode(
                        text = result.text,
                        format = result.barcodeFormat,
                        title = ""
                    )

                    val input = EditText(activity).apply {
                        setHint(R.string.dialog_card_name_hint)
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    }

                    AlertDialog.Builder(activity!!)
                        .setTitle(R.string.app_name)
                        .setView(input)
                        .setOnDismissListener { found.set(false) }
                        .setPositiveButton(R.string.action_add) { _, _ ->
                            presenter.addNewCode(
                                result.barcodeFormat,
                                result.text,
                                input.text.toString()
                            )
                        }
                        .setNegativeButton(R.string.action_cancel) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        }

        scanScanner.decodeContinuous(callback)
    }

    companion object {
        private const val REQUEST_CAMERA = 16
    }
}

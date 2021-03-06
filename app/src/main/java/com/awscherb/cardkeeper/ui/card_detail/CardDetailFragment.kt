package com.awscherb.cardkeeper.ui.card_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.awscherb.cardkeeper.R
import com.awscherb.cardkeeper.data.model.ScannedCode
import com.awscherb.cardkeeper.data.service.ScannedCodeService
import com.awscherb.cardkeeper.ui.base.BaseFragment
import com.awscherb.cardkeeper.ui.create.*
import com.google.zxing.BarcodeFormat.*
import com.google.zxing.WriterException
import com.jakewharton.rxbinding3.widget.textChanges
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.fragment_card_detail.*
import javax.inject.Inject

class CardDetailFragment : BaseFragment() {

    private val viewModel by viewModels<CardDetailViewModel> {
        CardDetailViewModelFactory(
            CardDetailFragmentArgs.fromBundle(arguments!!).cardId,
            scannedCodeService
        )
    }

    @Inject
    lateinit var scannedCodeService: ScannedCodeService

    private val encoder: BarcodeEncoder = BarcodeEncoder()

    //================================================================================
    // Lifecycle methods
    //================================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_card_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewComponent.inject(this)

        cardDetailSave.setOnClickListener { viewModel.save() }

        viewModel.card.observe(viewLifecycleOwner, Observer(this::showCard))
        viewModel.saveResult.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.onSaveResult(it)
            }
        })
    }


    //================================================================================
    // View methods
    //================================================================================

    private fun showCard(code: ScannedCode) {
        // Set title
        cardDetailTitle.setText(code.title)
        cardDetailText.text = code.text

        toolbar.title = code.title

        // Set image scaleType according to barcode type
        val scaleType = when (code.format) {
            QR_CODE, AZTEC, DATA_MATRIX -> ImageView.ScaleType.FIT_CENTER
            else -> ImageView.ScaleType.FIT_XY
        }
        cardDetailImage.scaleType = scaleType

        // Load image
        try {
            cardDetailImage.setImageBitmap(
                encoder.encodeBitmap(code.text, code.format, 200, 200)
            )
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        addDisposable(
            cardDetailTitle.textChanges()
                .map { it.toString() }
                .compose(bindToLifecycle())
                .subscribe(
                    viewModel.title::postValue,
                    this::onError
                )
        )
    }

    private fun onSaveResult(result: SaveResult) {
        when (result) {
            InvalidTitle -> showSnackbar(R.string.fragment_create_invalid_title)
            InvalidText -> showSnackbar(R.string.fragment_create_invalid_text)
            InvalidFormat -> showSnackbar(R.string.fragment_create_invalid_format)
            is SaveSuccess -> onCardSaved()
        }
        viewModel.saveResult.postValue(null)
    }


    private fun setSaveVisible(visible: Boolean) {
        cardDetailSave.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun onCardSaved() {
        showSnackbar(R.string.fragment_card_detail_saved)
        activity?.onBackPressed()
    }

}

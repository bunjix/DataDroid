/**
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package com.foxykeep.datadroidpoc.ui.crud;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.foxykeep.datadroidpoc.R;
import com.foxykeep.datadroidpoc.data.model.Phone;
import com.foxykeep.datadroidpoc.data.requestmanager.PoCRequestManager;
import com.foxykeep.datadroidpoc.data.service.PoCService;
import com.foxykeep.datadroidpoc.dialogs.ConnexionErrorDialogFragment;
import com.foxykeep.datadroidpoc.dialogs.ProgressDialogFragment;
import com.foxykeep.datadroidpoc.dialogs.ProgressDialogFragment.ProgressDialogFragmentBuilder;
import com.foxykeep.datadroidpoc.ui.DataDroidActivity;
import com.foxykeep.datadroidpoc.util.UserManager;

public final class CrudSyncPhoneAddEditActivity extends DataDroidActivity implements
        RequestListener, OnClickListener, TextWatcher {

    public static final String INTENT_EXTRA_PHONE = "com.foxykeep.datadroidpoc.ui.extras.phone";

    private EditText mEditTextName;
    private EditText mEditTextManufacturer;
    private EditText mEditTextAndroidVersion;
    private EditText mEditTextScreenSize;
    private EditText mEditTextPrice;
    private Button mButtonSubmit;
    private String mUserId;

    private Phone mPhone;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.crud_phone_add_edit);
        bindViews();

        mUserId = UserManager.getUserId(this);

        final Intent intent = getIntent();
        if (intent != null) {
            mPhone = intent.getParcelableExtra(INTENT_EXTRA_PHONE);
        }

        setTitle(mPhone == null ? R.string.crud_sync_phone_add_title
                : R.string.crud_sync_phone_edit_title);

        populateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0, length = mRequestList.size(); i < length; i++) {
            Request request = mRequestList.get(i);

            if (mRequestManager.isRequestInProgress(request)) {
                mRequestManager.addOnRequestFinishedListener(this, request);
            } else {
                int requestType = request.getRequestType();
                if (requestType == PoCService.WORKER_TYPE_CRUD_SYNC_PHONE_ADD) {
                    Intent resultData = new Intent();
                    resultData.putExtra(CrudSyncPhoneListActivity.RESULT_EXTRA_ADDED_PHONE,
                            mRequestManager.getMemoryProvider().syncPhoneAddedEditedPhone);
                    setResult(RESULT_OK, resultData);
                    finish();
                } else if (requestType == PoCService.WORKER_TYPE_CRUD_SYNC_PHONE_EDIT) {
                    Intent resultData = new Intent();
                    resultData.putExtra(CrudSyncPhoneListActivity.RESULT_EXTRA_EDITED_PHONE,
                            mRequestManager.getMemoryProvider().syncPhoneAddedEditedPhone);
                    setResult(RESULT_OK, resultData);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mRequestList.isEmpty()) {
            mRequestManager.removeOnRequestFinishedListener(this);
        }
    }

    private void bindViews() {
        mEditTextName = (EditText) findViewById(R.id.et_name);
        mEditTextName.addTextChangedListener(this);
        mEditTextManufacturer = (EditText) findViewById(R.id.et_manufacturer);
        mEditTextManufacturer.addTextChangedListener(this);
        mEditTextAndroidVersion = (EditText) findViewById(R.id.et_android_version);
        mEditTextAndroidVersion.addTextChangedListener(this);
        mEditTextScreenSize = (EditText) findViewById(R.id.et_screen_size);
        mEditTextScreenSize.addTextChangedListener(this);
        mEditTextPrice = (EditText) findViewById(R.id.et_price);
        mEditTextPrice.addTextChangedListener(this);

        mButtonSubmit = (Button) findViewById(R.id.b_submit);
        mButtonSubmit.setOnClickListener(this);
    }

    private void populateViews() {
        if (mPhone != null) {
            mEditTextName.setText(mPhone.name);
            mEditTextManufacturer.setText(mPhone.manufacturer);
            mEditTextAndroidVersion.setText(mPhone.androidVersion);
            mEditTextScreenSize.setText(String.valueOf(mPhone.screenSize));
            mEditTextPrice.setText(String.valueOf(mPhone.price));
        }
    }

    private void callSyncPhoneAddWS() {
        new ProgressDialogFragmentBuilder(this)
                .setMessage(R.string.progress_dialog_message)
                .setCancelable(true)
                .show();
        Request request = mRequestManager.addSyncPhone(
                mUserId,
                mEditTextName.getText().toString(),
                mEditTextManufacturer.getText().toString(),
                mEditTextAndroidVersion.getText().toString(),
                Double.parseDouble(mEditTextScreenSize.getText().toString()),
                Integer.parseInt(mEditTextPrice.getText().toString()));
        mRequestManager.addOnRequestFinishedListener(this, request);
        mRequestList.add(request);
    }

    private void callSyncPhoneEditWS() {
        new ProgressDialogFragmentBuilder(this)
                .setMessage(R.string.progress_dialog_message)
                .setCancelable(true)
                .show();
        Request request = mRequestManager.editSyncPhone(
                mUserId,
                mPhone.serverId,
                mEditTextName.getText().toString(),
                mEditTextManufacturer.getText().toString(),
                mEditTextAndroidVersion.getText().toString(),
                Double.parseDouble(mEditTextScreenSize.getText().toString()),
                Integer.parseInt(mEditTextPrice.getText().toString()));
        mRequestManager.addOnRequestFinishedListener(this, request);
        mRequestList.add(request);
    }

    @Override
    public void onClick(final View view) {
        if (view == mButtonSubmit) {
            if (mPhone == null) {
                callSyncPhoneAddWS();
            } else {
                callSyncPhoneEditWS();
            }
        }
    }

    @Override
    public void afterTextChanged(final Editable s) {
        mButtonSubmit.setEnabled(!TextUtils.isEmpty(mEditTextName.getText().toString())
                && !TextUtils.isEmpty(mEditTextManufacturer.getText().toString())
                && !TextUtils.isEmpty(mEditTextAndroidVersion.getText().toString())
                && !TextUtils.isEmpty(mEditTextScreenSize.getText().toString())
                && !TextUtils.isEmpty(mEditTextPrice.getText().toString()));
    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count,
            final int after) {
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before,
            final int count) {
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        if (mRequestList.contains(request)) {
            ProgressDialogFragment.dismiss(this);
            mRequestList.remove(request);

            final int requestType = request.getRequestType();
            if (requestType == PoCService.WORKER_TYPE_CRUD_SYNC_PHONE_ADD) {
                Phone phone = resultData.getParcelable(
                        PoCRequestManager.RECEIVER_EXTRA_PHONE_ADD_EDIT_DATA);
                Intent intent = new Intent();
                intent.putExtra(CrudSyncPhoneListActivity.RESULT_EXTRA_ADDED_PHONE, phone);
                setResult(RESULT_OK, intent);
                finish();
            } else if (requestType == PoCService.WORKER_TYPE_CRUD_SYNC_PHONE_EDIT) {
                Phone phone = resultData.getParcelable(
                        PoCRequestManager.RECEIVER_EXTRA_PHONE_ADD_EDIT_DATA);
                Intent intent = new Intent();
                intent.putExtra(CrudSyncPhoneListActivity.RESULT_EXTRA_EDITED_PHONE, phone);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onRequestConnectionError(Request request) {
        if (mRequestList.contains(request)) {
            ProgressDialogFragment.dismiss(this);
            mRequestList.remove(request);

            ConnexionErrorDialogFragment.show(this, request, this);
        }
    }

    @Override
    public void onRequestDataError(Request request) {
        if (mRequestList.contains(request)) {
            ProgressDialogFragment.dismiss(this);
            mRequestList.remove(request);

            showBadDataErrorDialog();
        }
    }
}